package com.vres.entity;

import java.sql.Date;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="projects")
public class Projects {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private int id;
	@Column
	private String title;
	@Column
	private String description;
	@Column
	private Date start_date;
	@Column
	private Date end_date;
	@Column
	private String status;
	@Column
	private double voucher_points;
	@Column
	private Date voucher_valid_from;
	@Column
	private Date voucher_valid_till;
	
	@CreationTimestamp // This annotation automatically sets the timestamp when a user is created
    @Column(name = "created_at", nullable = false, updatable = false)	
	private LocalDate created_at;
	
	public Projects() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public double getVoucher_points() {
		return voucher_points;
	}

	public void setVoucher_points(double voucher_points) {
		this.voucher_points = voucher_points;
	}

	public Date getVoucher_valid_from() {
		return voucher_valid_from;
	}

	public void setVoucher_valid_from(Date voucher_valid_from) {
		this.voucher_valid_from = voucher_valid_from;
	}

	public Date getVoucher_valid_till() {
		return voucher_valid_till;
	}

	public void setVoucher_valid_till(Date voucher_valid_till) {
		this.voucher_valid_till = voucher_valid_till;
	}


	public Projects(int id, String title, String description, Date start_date, Date end_date,
			String status, double voucher_points, Date voucher_valid_from, Date voucher_valid_till, LocalDate created_at) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.start_date = start_date;
		this.end_date = end_date;
		this.status = status;
		this.voucher_points = voucher_points;
		this.voucher_valid_from = voucher_valid_from;
		this.voucher_valid_till = voucher_valid_till;
		this.created_at = created_at;
	}

	public LocalDate getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDate created_at) {
		this.created_at = created_at;
	}
	
	
}
