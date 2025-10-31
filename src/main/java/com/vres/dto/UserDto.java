package com.vres.dto;

import java.time.LocalDateTime;

public class UserDto {

    private int id;
    private String name;
    private Integer projectId;
    private int roleId;
    private String email;
    private String phone;
    private String vendorGst;
    private String vendorAddress;
    private String password;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Constructors
    public UserDto() {
    }

    public UserDto(int id, String name, Integer projectId, int roleId, String email, String phone, String vendorGst, String vendorAddress, String password, boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.projectId = projectId;
        this.roleId = roleId;
        this.email = email;
        this.phone = phone;
        this.vendorGst = vendorGst;
        this.vendorAddress = vendorAddress;
        this.password = password;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVendorGst() {
        return vendorGst;
    }

    public void setVendorGst(String vendorGst) {
        this.vendorGst = vendorGst;
    }

    public String getVendorAddress() {
        return vendorAddress;
    }

    public void setVendorAddress(String vendorAddress) {
        this.vendorAddress = vendorAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", projectId=" + projectId +
                ", roleId=" + roleId +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", vendorGst='" + vendorGst + '\'' +
                ", vendorAddress='" + vendorAddress + '\'' +
                ", password='[PROTECTED]'" +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
