package com.vres.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vres.entity.Redemptions;
import com.vres.entity.Users;
import com.vres.entity.Vouchers;
import com.vres.repository.ProjectUserRepository;
import com.vres.repository.RedemptionsRepository;
import com.vres.repository.UsersRepository;
import com.vres.repository.VouchersRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RedemptionService {

    @Autowired
    private RedemptionsRepository redemptionRepository;
    
    @Autowired
    private VouchersRepository vouchersRepository;
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private ProjectUserRepository projectUserRepository;
    
    @Value("${otp.validity.minutes:5}")
    private long otpValidityMinutes;
    
    @Autowired
    private SnsService snsService;

    public void initiateRedemption(String voucherCode, int vendorId) {
        Vouchers voucher = vouchersRepository.findByStringCode(voucherCode)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found with code: " + voucherCode));
        
        boolean isVendorValid = projectUserRepository.existsByProjectIdAndUserIdAndVendorStatus(voucher.getProject().getId(), vendorId, 1);
       
        if (!isVendorValid) {
            throw new SecurityException("Vendor with ID " + vendorId + " is not authorized for this project.");
        }

        if (!"ISSUED".equalsIgnoreCase(voucher.getStatus())) {
            throw new IllegalStateException("Voucher not available for redemption. Status: " + voucher.getStatus());
        }

        LocalDate today = LocalDate.now();
        LocalDate validFrom = voucher.getProject().getVoucher_valid_from().toLocalDate();
        LocalDate validTill = voucher.getProject().getVoucher_valid_till().toLocalDate();
        if (today.isBefore(validFrom) || today.isAfter(validTill)) {
            throw new IllegalStateException("Voucher is outside its validity period.");
        }

        String otp = String.format("%06d", (int) (Math.random() * 999999));
        voucher.setRedemptionOtp(otp);
        // --- TYPO FIXED ---
        voucher.setRedemptionOtpIssuedTime(LocalDateTime.now());
        // ------------------
        vouchersRepository.save(voucher);

        String beneficiaryPhone = voucher.getBeneficiary().getPhone();
        if (beneficiaryPhone != null && !beneficiaryPhone.isEmpty()) {
            String message = "Your OTP for voucher redemption is " + otp + ". It is valid for " + otpValidityMinutes + " minutes.";
            snsService.publishSmsDirect(beneficiaryPhone, message);
        }
        System.out.println("Redemption initiated for voucher: " + voucherCode + ", OTP: " + otp);
    }

    @Transactional
    public void confirmRedemption(String voucherCode, String otp, int vendorId, Double geoLat, Double geoLon, String deviceFingerprint) {
    	
    	final double DEFAULT_LAT = 0.0;
        final double DEFAULT_LON = 0.0;
        final String DEFAULT_FINGERPRINT = "default-device-fingerprint";

        if (geoLat == null) geoLat = DEFAULT_LAT;
        if (geoLon == null) geoLon = DEFAULT_LON;
        if (deviceFingerprint == null || deviceFingerprint.trim().isEmpty()) deviceFingerprint = DEFAULT_FINGERPRINT;
        
        Vouchers voucher = vouchersRepository.findByStringCode(voucherCode)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found with code: " + voucherCode));

        if (voucher.getRedemptionOtp() == null || !voucher.getRedemptionOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP for voucher: " + voucherCode);
        }
        
        // --- TYPO FIXED ---
        LocalDateTime otpExpiryTime = voucher.getRedemptionOtpIssuedTime().plusMinutes(otpValidityMinutes);
        if (LocalDateTime.now().isAfter(otpExpiryTime)) {
            voucher.setRedemptionOtp(null);
            voucher.setRedemptionOtpIssuedTime(null);
            // ------------------
            vouchersRepository.save(voucher);
            throw new RuntimeException("OTP has expired for voucher: " + voucherCode);
        }

        Users vendor = usersRepository.findById(vendorId)
        		.orElseThrow(() -> new EntityNotFoundException("Vendor not found with id: " + vendorId));
        
        if (!"ISSUED".equalsIgnoreCase(voucher.getStatus())) {
            throw new IllegalStateException("Voucher cannot be redeemed. Status: " + voucher.getStatus());
        }
        
        Redemptions redemption = new Redemptions();
        redemption.setVoucher(voucher);
        redemption.setVendor(vendor);
        redemption.setRedeemed_date(new Date(System.currentTimeMillis()));
        redemption.setGeo_lat(geoLat);
        redemption.setGeo_lon(geoLon);
        redemption.setDevice_fingerprint(deviceFingerprint);

        redemptionRepository.save(redemption);

        voucher.setStatus("REDEEMED");
        voucher.setRedemptionOtp(null);
        // --- TYPO FIXED ---
        voucher.setRedemptionOtpIssuedTime(null);
        // ------------------
        vouchersRepository.save(voucher);

        System.out.println("Redemption confirmed for voucher: " + voucherCode + " by vendor: " + vendor.getName());
    }
}