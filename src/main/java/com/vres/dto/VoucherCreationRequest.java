package com.vres.dto;

import java.time.LocalDate;
import java.util.List;

public class VoucherCreationRequest {
    private String title;
    // MODIFIED: Changed from List<String> to List<Integer> to match the API spec
    private List<Integer> vendors; 
    private double voucherPoints;
    private LocalDate validityStart;
    private LocalDate validityEnd;
    
    // This field is required by the workflow to specify which beneficiaries
    // should receive a voucher.
    private List<Integer> beneficiaryIds;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // MODIFIED: Getter and setter updated for List<Integer>
    public List<Integer> getVendors() { return vendors; }
    public void setVendors(List<Integer> vendors) { this.vendors = vendors; }

    public double getVoucherPoints() { return voucherPoints; }
    public void setVoucherPoints(double voucherPoints) { this.voucherPoints = voucherPoints; }
    public LocalDate getValidityStart() { return validityStart; }
    public void setValidityStart(LocalDate validityStart) { this.validityStart = validityStart; }
    public LocalDate getValidityEnd() { return validityEnd; }
    public void setValidityEnd(LocalDate validityEnd) { this.validityEnd = validityEnd; }
    public List<Integer> getBeneficiaryIds() { return beneficiaryIds; }
    public void setBeneficiaryIds(List<Integer> beneficiaryIds) { this.beneficiaryIds = beneficiaryIds; }
}

