package com.vres.dto;

import java.time.LocalDate;
import java.util.List;

public class ProjectDetailsCreationRequest {
    // Basic Project Info
    private String projectName;
    private String projectDescription;
    private LocalDate startDate;
    private LocalDate registrationEndDate;

    // Role Assignments
    private List<ApproverPairDto> approvers; // For Maker/Checker pairs
    private List<Integer> issuerIds;         // For Issuers (list of user IDs)
    private List<VendorAssignmentDto> vendors; // For Vendors (list of objects with details)

    // Getters and Setters
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getRegistrationEndDate() { return registrationEndDate; }
    public void setRegistrationEndDate(LocalDate registrationEndDate) { this.registrationEndDate = registrationEndDate; }

    public List<ApproverPairDto> getApprovers() { return approvers; }
    public void setApprovers(List<ApproverPairDto> approvers) { this.approvers = approvers; }

    public List<Integer> getIssuerIds() { return issuerIds; }
    public void setIssuerIds(List<Integer> issuerIds) { this.issuerIds = issuerIds; }

    public List<VendorAssignmentDto> getVendors() { return vendors; }
    public void setVendors(List<VendorAssignmentDto> vendors) { this.vendors = vendors; }
}