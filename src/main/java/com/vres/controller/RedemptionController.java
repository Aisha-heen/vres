package com.vres.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vres.dto.GenericResponse;
import com.vres.dto.VoucherConfirmRedemptionRequest;
import com.vres.dto.VoucherInitiateRedemptionRequest;
import com.vres.service.RedemptionService;

@RestController
@RequestMapping("/vres/redemption")
public class RedemptionController {

    @Autowired
    private RedemptionService redemptionService;

    @PostMapping("/initiate")
    public ResponseEntity<GenericResponse> initiateRedemption(@RequestBody VoucherInitiateRedemptionRequest request) {
    	redemptionService.initiateRedemption(request.getVoucherCode(), request.getVendorId());
        return ResponseEntity.ok(new GenericResponse("Redemption initiated, OTP sent to beneficiary."));
    }
    

    @PostMapping("/confirm")
    public ResponseEntity<GenericResponse> confirmRedemption(@RequestBody VoucherConfirmRedemptionRequest request) {
        redemptionService.confirmRedemption(request.getVoucherCode(), request.getOtp(), request.getVendorId(), request.getGeo_lat(), request.getGeo_lon(), request.getDeviceFingerprint());
        return ResponseEntity.ok(new GenericResponse("Voucher redeemed successfully."));
    }
}

