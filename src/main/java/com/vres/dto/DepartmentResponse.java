package com.vres.dto;

public class DepartmentResponse {
    private int departmentId;
    private String department;
    private Integer checkerUserId;
    private Integer makerUserId;
    private String address;

    // Getters and Setters
    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    // ✅ Null-safe getter for checkerUserId
    public Integer getCheckerUserId() {
        return checkerUserId;
    }

    public void setCheckerUserId(Integer checkerUserId) {
        this.checkerUserId = checkerUserId;
    }

    // ✅ Null-safe getter for makerUserId
    public Integer getMakerUserId() {
        return makerUserId;
    }

    public void setMakerUserId(Integer makerUserId) {
        this.makerUserId = makerUserId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}