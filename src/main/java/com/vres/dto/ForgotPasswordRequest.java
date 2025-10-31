package com.vres.dto;

public class ForgotPasswordRequest {

    private String email;

    // This getter is what the AuthController needs
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}