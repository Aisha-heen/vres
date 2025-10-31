package com.vres.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectDto {

    private int id;
    private String title;
    private String description;
    private Integer projectCoordinator;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private BigDecimal voucherPoints;
    private LocalDate voucherValidFrom;
    private LocalDate voucherValidTill;
    private LocalDateTime createdAt;

    // Constructors
    public ProjectDto() {
    }

    public ProjectDto(int id, String title, String description, Integer projectCoordinator, LocalDate startDate, LocalDate endDate, String status, BigDecimal voucherPoints, LocalDate voucherValidFrom, LocalDate voucherValidTill, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.projectCoordinator = projectCoordinator;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.voucherPoints = voucherPoints;
        this.voucherValidFrom = voucherValidFrom;
        this.voucherValidTill = voucherValidTill;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public Integer getProjectCoordinator() {
        return projectCoordinator;
    }

    public void setProjectCoordinator(Integer projectCoordinator) {
        this.projectCoordinator = projectCoordinator;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getVoucherPoints() {
        return voucherPoints;
    }

    public void setVoucherPoints(BigDecimal voucherPoints) {
        this.voucherPoints = voucherPoints;
    }

    public LocalDate getVoucherValidFrom() {
        return voucherValidFrom;
    }

    public void setVoucherValidFrom(LocalDate voucherValidFrom) {
        this.voucherValidFrom = voucherValidFrom;
    }

    public LocalDate getVoucherValidTill() {
        return voucherValidTill;
    }

    public void setVoucherValidTill(LocalDate voucherValidTill) {
        this.voucherValidTill = voucherValidTill;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ProjectDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", projectCoordinator=" + projectCoordinator +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", voucherPoints=" + voucherPoints +
                ", voucherValidFrom=" + voucherValidFrom +
                ", voucherValidTill=" + voucherValidTill +
                ", createdAt=" + createdAt +
                '}';
    }
}
