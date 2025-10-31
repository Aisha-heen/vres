package com.vres.dto;

import java.time.LocalDate;

// A new DTO to hold the combined Voucher and Beneficiary details
public class ProjectVoucherDto {

    // Voucher Details
    private String qrCodeLink;
    private String voucherStatus;
    private String stringCode;
    private LocalDate issuedAt;

    // Beneficiary Details
    private String beneficiaryName;
    private String beneficiaryPhone;
    private boolean beneficiaryIsApproved;

    // Getters and Setters
    
    public String getQrCodeLink() {
        return qrCodeLink;
    }

    public void setQrCodeLink(String qrCodeLink) {
        this.qrCodeLink = qrCodeLink;
    }

    public String getVoucherStatus() {
        return voucherStatus;
    }

    public void setVoucherStatus(String voucherStatus) {
        this.voucherStatus = voucherStatus;
    }

    public String getStringCode() {
        return stringCode;
    }

    public void setStringCode(String stringCode) {
        this.stringCode = stringCode;
    }

    public LocalDate getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDate issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getBeneficiaryPhone() {
        return beneficiaryPhone;
    }

    public void setBeneficiaryPhone(String beneficiaryPhone) {
        this.beneficiaryPhone = beneficiaryPhone;
    }

    public boolean isBeneficiaryIsApproved() {
        return beneficiaryIsApproved;
    }

    public void setBeneficiaryIsApproved(boolean beneficiaryIsApproved) {
        this.beneficiaryIsApproved = beneficiaryIsApproved;
    }
}