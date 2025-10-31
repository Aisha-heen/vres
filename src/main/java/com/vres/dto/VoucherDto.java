package com.vres.dto;

import java.time.LocalDateTime;

public class VoucherDto {

    private int id;
    private int projectId;
    private int beneficiaryId;
    private String stringCode;
    private String qrCode;
    private String status;
    private LocalDateTime issuedAt;

    // Constructors
    public VoucherDto() {
    }

    public VoucherDto(int id, int projectId, int beneficiaryId, String stringCode, String qrCode, String status, LocalDateTime issuedAt) {
        this.id = id;
        this.projectId = projectId;
        this.beneficiaryId = beneficiaryId;
        this.stringCode = stringCode;
        this.qrCode = qrCode;
        this.status = status;
        this.issuedAt = issuedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(int beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public String getStringCode() {
        return stringCode;
    }

    public void setStringCode(String stringCode) {
        this.stringCode = stringCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    @Override
    public String toString() {
        return "VoucherDto{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", beneficiaryId=" + beneficiaryId +
                ", stringCode='" + stringCode + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", status='" + status + '\'' +
                ", issuedAt=" + issuedAt +
                '}';
    }
}
