package com.vres.entity;



import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vouchers")
public class Vouchers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Projects project;

    @ManyToOne
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiaries beneficiary;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "string_code", unique = true, nullable = false)
    private String stringCode;

    @Column(name = "qr_code_link", columnDefinition = "TEXT")
    private String qrCodeLink;

    @Column(name = "redemption_otp")
    private String redemptionOtp;

    @Column(name = "redemption_otp_issued_time")
    private LocalDateTime redemptionOtpIssuedTime;
    
    @CreationTimestamp
    @Column(name = "issued_at")
    private LocalDate issuedAt;

    public LocalDate getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(LocalDate issuedAt) {
		this.issuedAt = issuedAt;
	}

	// Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Projects getProject() {
        return project;
    }

    public void setProject(Projects project) {
        this.project = project;
    }

    public Beneficiaries getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Beneficiaries beneficiary) {
        this.beneficiary = beneficiary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStringCode() {
        return stringCode;
    }

    public void setStringCode(String stringCode) {
        this.stringCode = stringCode;
    }

    public String getQrCodeLink() {
        return qrCodeLink;
    }

    public void setQrCodeLink(String qrCodeLink) {
        this.qrCodeLink = qrCodeLink;
    }

    public String getRedemptionOtp() {
        return redemptionOtp;
    }

    public void setRedemptionOtp(String redemptionOtp) {
        this.redemptionOtp = redemptionOtp;
    }

    public LocalDateTime getRedemptionOtpIssuedTime() {
        return redemptionOtpIssuedTime;
    }

    public void setRedemptionOtpIssuedTime(LocalDateTime redemptionOtpIssuedTime) {
        this.redemptionOtpIssuedTime = redemptionOtpIssuedTime;
    }
}