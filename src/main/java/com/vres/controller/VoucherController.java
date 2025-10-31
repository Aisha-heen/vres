package com.vres.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // Import PostMapping
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.vres.service.VoucherService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/vres/vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping(value = "/{voucherId}/code", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getVoucherCode(@PathVariable Integer voucherId) {
        try {
            String uniqueCode = voucherService.getVoucherCodeById(voucherId);
            if (uniqueCode == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(uniqueCode);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found.");
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.err.println("Code retrieval failed for ID " + voucherId + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error during code retrieval.");
        }
    }

    @GetMapping(value = "/{voucherId}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCodeImageBytes(@PathVariable Integer voucherId) {
        try {
            byte[] qrCodeBytes = voucherService.getQrCodeImageBytes(voucherId);
            if (qrCodeBytes == null || qrCodeBytes.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header("Content-Disposition", "inline; filename=\"voucher-" + voucherId + ".png\"")
                    .body(qrCodeBytes);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher or QR Code not found for ID: " + voucherId);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.err.println("QR Code retrieval failed for voucher ID " + voucherId + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while retrieving QR code for voucher ID: " + voucherId);
        }
    }

    @PostMapping(value = "/{voucherId}/issue-otp", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> issueOtp(@PathVariable Integer voucherId) {
        try {
            String otp = voucherService.issueOtp(voucherId);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(otp);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found.");
        } catch (IllegalStateException e) {
            // This will catch validation errors like "Voucher not ISSUED" or "Voucher not yet valid"
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.err.println("OTP issue failed for voucher ID " + voucherId + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while issuing OTP.");
        }
    }
}