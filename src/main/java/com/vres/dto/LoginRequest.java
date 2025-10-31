package com.vres.dto;

public class LoginRequest {
    // This MUST be 'userId' to match the JSON from your React app
    private String userId; 
    private String password;

    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}