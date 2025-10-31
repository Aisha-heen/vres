package com.vres.dto;

public class ResetPasswordRequest {

    private String email;
    private String otp;
    private String newPassword;

    // Getters and Setters are required for Spring to map the JSON data
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}