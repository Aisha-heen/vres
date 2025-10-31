package com.vres.dto;

import java.util.List;

public class ProjectDashboardDTO {

    private Long projectId;
    private String projectName;
    private String departmentName;
    private String projectDescription;

    private List<BeneficiaryDto> beneficiaries;
    public Long getProjectId() {
		return projectId;

    // Getters and Setters
}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public String getProjectDescription() {
		return projectDescription;
	}
	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}
	public List<BeneficiaryDto> getBeneficiaries() {
		return beneficiaries;
	}
	public void setBeneficiaries(List<BeneficiaryDto> beneficiaries) {
		this.beneficiaries = beneficiaries;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
}