package com.vres.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_user")
public class ProjectUser {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    // --- THIS IS THE FIX ---
    // Changed nullable from 'false' to 'true' to match our workflow.
    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Projects project;
    // -----------------------

    @Column(name = "vendor_status")
    private Integer vendorStatus;

    @ManyToOne
    @JoinColumn(name = "role_id")
	private Roles role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public ProjectUser() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Projects getProject() { return project; }
    public void setProject(Projects project) { this.project = project; }
	public Integer getVendorStatus() { return vendorStatus; }
	public void setVendorStatus(Integer vendorStatus) { this.vendorStatus = vendorStatus; }
    public Roles getRole() { return role; }
    public void setRole(Roles role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}