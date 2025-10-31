package com.vres.dto;

import java.time.LocalDate;
import java.util.List;

public class ProjectDashboardResponse {
    private int projectId;
    private String title;
    private LocalDate creationDate;
    private List<String> vendors;
    private double voucherPoints;
    private LocalDate voucherValidityStartDate;
    private LocalDate voucherValidityEndDate;
    private VoucherStatusSummary voucherStatusSummary;
    private List<BeneficiaryStatus> beneficiaries;
    
    // Nested static classes for complex properties
    public static class VoucherStatusSummary {
        private String issued;
        private String redeemed;
        private String expired;
        private String cancelled;
        // Getters and Setters
        public String getIssued() { return issued; }
        public void setIssued(String issued) { this.issued = issued; }
        public String getRedeemed() { return redeemed; }
        public void setRedeemed(String redeemed) { this.redeemed = redeemed; }
        public String getExpired() { return expired; }
        public void setExpired(String expired) { this.expired = expired; }
        public String getCancelled() { return cancelled; }
        public void setCancelled(String cancelled) { this.cancelled = cancelled; }
    }
    
    public static class BeneficiaryStatus {
        private String beneficiaryName;
        private String voucherStatus;
        private LocalDate redemptionDate;
        // Getters and Setters
        public String getBeneficiaryName() { return beneficiaryName; }
        public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
        public String getVoucherStatus() { return voucherStatus; }
        public void setVoucherStatus(String voucherStatus) { this.voucherStatus = voucherStatus; }
        public LocalDate getRedemptionDate() { return redemptionDate; }
        public void setRedemptionDate(LocalDate redemptionDate) { this.redemptionDate = redemptionDate; }
    }

    // Getters and Setters for ProjectDashboardResponse
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
    public List<String> getVendors() { return vendors; }
    public void setVendors(List<String> vendors) { this.vendors = vendors; }
    public double getVoucherPoints() { return voucherPoints; }
    public void setVoucherPoints(double voucherPoints) { this.voucherPoints = voucherPoints; }
    public LocalDate getVoucherValidityStartDate() { return voucherValidityStartDate; }
    public void setVoucherValidityStartDate(LocalDate voucherValidityStartDate) { this.voucherValidityStartDate = voucherValidityStartDate; }
    public LocalDate getVoucherValidityEndDate() { return voucherValidityEndDate; }
    public void setVoucherValidityEndDate(LocalDate voucherValidityEndDate) { this.voucherValidityEndDate = voucherValidityEndDate; }
    public VoucherStatusSummary getVoucherStatusSummary() { return voucherStatusSummary; }
    public void setVoucherStatusSummary(VoucherStatusSummary voucherStatusSummary) { this.voucherStatusSummary = voucherStatusSummary; }
    public List<BeneficiaryStatus> getBeneficiaries() { return beneficiaries; }
    public void setBeneficiaries(List<BeneficiaryStatus> beneficiaries) { this.beneficiaries = beneficiaries; }
}
