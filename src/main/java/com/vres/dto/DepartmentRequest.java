package com.vres.dto;

// Renamed from SponsorRequest
public class DepartmentRequest {
    private String department;
    private int checkerUserId;
    private int makerUserId;
    private String address;

    // Getters and Setters
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getCheckerUserId() {
        return checkerUserId;
    }

    public void setCheckerUserId(int checkerUserId) {
        this.checkerUserId = checkerUserId;
    }

    public int getMakerUserId() {
        return makerUserId;
    }

    public void setMakerUserId(int makerUserId) {
        this.makerUserId = makerUserId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
