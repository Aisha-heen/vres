package com.vres.dto;

import java.time.LocalDateTime;
import java.util.List;

// This DTO is designed for the User Dashboard View (Admin/Super Admin)
public class UserDashboardDto {

    private int id;
    private String name;
    private String email;
    private String phone;
    private boolean isActive;
    private LocalDateTime createdAt;
    
    // Vendor specific fields (optional)
    private String vendorGst;
    private String vendorAddress;

    // A list of all roles/projects this user is associated with
    // This is the key difference from a simple UserResponse
    private List<UserRoleProjectLink> assignments; 

    // Constructor, Getters, and Setters...

    public UserDashboardDto(int id, String name, String email, String phone, boolean isActive, LocalDateTime createdAt, String vendorGst, String vendorAddress) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.vendorGst = vendorGst;
        this.vendorAddress = vendorAddress;
    }

    // Nested DTO to show role and project links
    public static class UserRoleProjectLink {
        private String roleName;
        private String projectName; // Null for system-wide roles like 'Project Coordinator'

        public UserRoleProjectLink(String roleName, String projectName) {
            this.roleName = roleName;
            this.projectName = projectName;
        }

        // Getters and Setters
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
    }


    // Standard Getters and Setters for UserDashboardDto
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getVendorGst() { return vendorGst; }
    public void setVendorGst(String vendorGst) { this.vendorGst = vendorGst; }
    public String getVendorAddress() { return vendorAddress; }
    public void setVendorAddress(String vendorAddress) { this.vendorAddress = vendorAddress; }
    public List<UserRoleProjectLink> getAssignments() { return assignments; }
    public void setAssignments(List<UserRoleProjectLink> assignments) { this.assignments = assignments; }
}