package com.vres.dto;

/**
 * DTO for the new, simplified user registration process.
 * It only contains the essential fields to create a user and associate them with a project.
 * Role, GST, and Address are no longer included here.
 */
public class UserRegistrationRequest {

    private String email;
    private String name;
    private String phone;
    private Integer projectId;

    // Standard Getters and Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
}
