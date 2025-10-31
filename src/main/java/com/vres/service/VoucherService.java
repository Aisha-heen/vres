package com.vres.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import com.vres.entity.Vouchers;
import com.vres.repository.VouchersRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class VoucherService {

    private static final Logger logger = LoggerFactory.getLogger(VoucherService.class);

    @Autowired
    private VouchersRepository vouchersRepository;

    @Autowired
    private S3Service s3Service; // Assuming S3Service is correctly configured

    @Autowired
    private OtpService otpService; // Assuming OtpService is correctly configured

    /**
     * Validates the voucher before performing critical operations.
     * Checks status, project link, and validity dates.
     * Throws IllegalStateException if validation fails.
     */
    private void validateVoucher(Vouchers voucher) {
        logger.debug("Validating voucher ID: {}", voucher.getId());
        if (voucher.getStatus() == null || !"ISSUED".equalsIgnoreCase(voucher.getStatus())) {
             logger.warn("Validation failed for voucher ID {}: Status is not ISSUED (Status: {})", voucher.getId(), voucher.getStatus());
            throw new IllegalStateException("Voucher status must be ISSUED to proceed.");
        }

        LocalDate today = LocalDate.now();
        if (voucher.getProject() == null) {
            logger.error("Validation failed for voucher ID {}: Project link is missing.", voucher.getId());
            throw new IllegalStateException("Voucher's project is missing.");
        }

        // Check project's voucher validity dates
        if (voucher.getProject().getVoucher_valid_from() == null || voucher.getProject().getVoucher_valid_till() == null) {
             logger.error("Validation failed for voucher ID {}: Project {} validity dates are missing.", voucher.getId(), voucher.getProject().getId());
            throw new IllegalStateException("Voucher's project validity dates are missing.");
        }

        LocalDate validFrom = voucher.getProject().getVoucher_valid_from().toLocalDate();
        LocalDate validTill = voucher.getProject().getVoucher_valid_till().toLocalDate();

        if (validFrom.isAfter(today)) {
             logger.warn("Validation failed for voucher ID {}: Voucher not yet valid (Starts: {})", voucher.getId(), validFrom);
            throw new IllegalStateException("Voucher is not yet valid (validity period starts on " + validFrom + ").");
        }

        if (validTill.isBefore(today)) {
             logger.warn("Validation failed for voucher ID {}: Voucher expired (Ended: {})", voucher.getId(), validTill);
            throw new IllegalStateException("Voucher has expired (validity period ended on " + validTill + ").");
        }
         logger.debug("Voucher ID {} passed validation.", voucher.getId());
    }

    /**
     * Returns the QR code link of a voucher if valid.
     * Marked as read-only transaction.
     */
    @Transactional(readOnly = true) // Read operation
    public String getVoucherQrLink(Integer voucherId) {
        logger.info("Fetching QR link for voucher ID: {}", voucherId);
        Vouchers voucher = vouchersRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found with ID: " + voucherId));

        // Validation happens internally, throws exception if invalid
        validateVoucher(voucher);
        logger.debug("QR link found for voucher ID {}: {}", voucherId, voucher.getQrCodeLink());
        return voucher.getQrCodeLink();
    }

    /**
     * Returns the string code of a voucher if valid.
     * Marked as read-only transaction.
     */
    @Transactional(readOnly = true) // Read operation
    public String getVoucherCodeById(Integer voucherId) {
        logger.info("Fetching string code for voucher ID: {}", voucherId);
        Vouchers voucher = vouchersRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found with ID: " + voucherId));

        validateVoucher(voucher);
        logger.debug("String code found for voucher ID {}: {}", voucherId, voucher.getStringCode());
        return voucher.getStringCode();
    }

    /**
     * Fetches the QR code image bytes from S3 if the voucher is valid.
     * Marked as read-only transaction regarding DB access.
     */
    @Transactional(readOnly = true) // Read operation (for voucher lookup)
    public byte[] getQrCodeImageBytes(Integer voucherId) {
        logger.info("Fetching QR code image bytes for voucher ID: {}", voucherId);
        Vouchers voucher = vouchersRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found with ID: " + voucherId));

        validateVoucher(voucher); // Check voucher validity

        String qrLink = voucher.getQrCodeLink();
        if (qrLink == null || qrLink.isBlank()) { // Use isBlank() for better check
             logger.error("QR code link is missing for voucher ID: {}", voucherId);
            throw new EntityNotFoundException("QR code link not found for voucher ID: " + voucherId);
        }

        try {
             logger.debug("Downloading QR code from S3 link: {}", qrLink);
            byte[] qrBytes = s3Service.downloadFileAsBytes(qrLink);
             logger.info("Successfully fetched QR code bytes for voucher ID {}", voucherId);
             return qrBytes;
        } catch (Exception e) {
             logger.error("Failed to fetch QR code from S3 for voucher ID {}: {}", voucherId, e.getMessage(), e);
            // Wrap S3 exception in a runtime exception
            throw new RuntimeException("Failed to fetch QR code from S3 for voucher ID: " + voucherId, e);
        }
    }

    /**
     * Issues a new OTP for a voucher, saves it, and returns the OTP.
     * This is a write operation, requires a transaction.
     */
    @Transactional // Write operation
    public String issueOtp(Integer voucherId) {
        logger.info("Issuing OTP for voucher ID: {}", voucherId);
        Vouchers voucher = vouchersRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found with ID: " + voucherId));

        validateVoucher(voucher); // Ensure voucher is valid before issuing OTP

        String otp = otpService.generateOtp();
        voucher.setRedemptionOtp(otp);
        voucher.setRedemptionOtpIssuedTime(LocalDateTime.now()); // Record issue time

        vouchersRepository.save(voucher); // Save the updated voucher with OTP

        logger.info("OTP issued successfully for voucher ID {}", voucherId);
        // Be cautious about logging the actual OTP value in production environments
        // logger.debug("OTP issued for voucher ID {}: {}", voucherId, otp);
        return otp;
    }

    /**
     * Returns the count of vouchers grouped by their status for a given project.
     * Marked as read-only transaction.
     */
    @Transactional(readOnly = true) // Read operation
    public Map<String, Long> getVoucherStatusCountByProject(Integer projectId) {
        logger.info("Fetching voucher status count for project ID: {}", projectId);
        // Find vouchers specifically by project ID for efficiency
        List<Vouchers> vouchers = vouchersRepository.findByProjectId(projectId);

        if (vouchers.isEmpty()) {
            logger.info("No vouchers found for project ID: {}", projectId);
            // Return an empty map instead of Map.of() for consistency if needed
            return Collections.emptyMap();
        }

        // Group by status (convert null status to "UNKNOWN") and count
        Map<String, Long> statusCount = vouchers.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getStatus() != null ? v.getStatus().toUpperCase() : "UNKNOWN", // Handle null status
                        Collectors.counting()
                ));

        logger.info("Voucher status count calculated for project {}: {}", projectId, statusCount);
        return statusCount;
    }
}