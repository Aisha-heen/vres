package com.vres.dto;

import java.util.List;

public class LoginResponse {
    
    private Integer userId;
    private String name;
    private String email;
    private String role; // <-- ADDED THIS FIELD BACK FOR THE FRONTEND
    private List<ProjectSummaryDto> projects;
    private String jwtToken;
    
    // Default constructor
    public LoginResponse() {}

    // Constructor from your AuthService
    public LoginResponse(Integer userId, String name, String email, List<ProjectSummaryDto> projects) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.projects = projects;
    }
    
    // --- Getters and Setters for all fields ---
    
    public Integer getUserId() { 
        return userId; 
    }
    public void setUserId(Integer userId) { 
        this.userId = userId; 
    }
    
    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public List<ProjectSummaryDto> getProjects() { 
        return projects; 
    } 
    public void setProjects(List<ProjectSummaryDto> projects) { 
        this.projects = projects; 
    } 
    
    // --- Getter/Setter for NEW field ---
    
    public String getJwtToken() { 
        return jwtToken; 
    }
    public void setJwtToken(String jwtToken) { 
        this.jwtToken = jwtToken; 
    }

    // --- GETTER/SETTER FOR THE ROLE FIELD ---
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}