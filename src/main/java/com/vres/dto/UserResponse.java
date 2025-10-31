package com.vres.dto;

public class UserResponse {
    private int userId;
    private String name;
    private String role;
    private String email;
    private String phone;

    public UserResponse(int userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    // --- CHANGE START ---
    // Added a full constructor for more flexible object creation.
    public UserResponse(int userId, String name, String role, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.email = email;
        this.phone = phone;
    }
    // --- CHANGE END ---

    public UserResponse() {}

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

