package com.vres.dto;

public class ProjectSummaryDto {
    private int projectId;
    private String projectName;
    private Integer departmentId;
    private String role;

    // Constructors
    public ProjectSummaryDto() {}

    public ProjectSummaryDto(int projectId, String projectName, Integer departmentId, String role) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.departmentId = departmentId;
        this.role = role;
    }

    // Getters and Setters
    public int getProjectId() { return projectId; }
    public String getRole() { return role;	}
	public void setRole(String role) {	this.role = role;	}
	public void setProjectId(int projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public Integer getDepartmentId() { return departmentId; } // <-- ADDED
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; } // <-- ADDED
}