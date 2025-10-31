package com.vres.entity;

import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="beneficiaries")
public class Beneficiaries {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private int id;
	@Column
	private String name;
	@Column
	private String phone;
	@ManyToOne
    @JoinColumn(name = "department_id")
	private Department department;
	@ManyToOne
    @JoinColumn(name = "project_id")
	private Projects project;
	@Column
	private boolean is_approved;
	@CreationTimestamp // This annotation automatically sets the timestamp when a user is created
    @Column(name = "created_at", nullable = false, updatable = false)
	private LocalDate created_at;
	
	public Beneficiaries() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Projects getProject() {
		return project;
	}

	public void setProject(Projects project) {
		this.project = project;
	}

	public boolean isIs_approved() {
		return is_approved;
	}

	public void setIs_approved(boolean is_approved) {
		this.is_approved = is_approved;
	}

	public LocalDate getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDate created_at) {
		this.created_at = created_at;
	}

	public Beneficiaries(int id, String name, String phone, Department department, Projects project,
			boolean is_approved, LocalDate created_at) {
		super();
		this.id = id;
		this.name = name;
		this.phone = phone;
		this.department = department;
		this.project = project;
		this.is_approved = is_approved;
		this.created_at = created_at;
	}

}
