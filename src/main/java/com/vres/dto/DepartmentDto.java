package com.vres.dto;

import java.time.LocalDateTime;

public class DepartmentDto {

    private int id;
    private Integer makerId;
    private Integer checkerId;
    private int projectId;
    private LocalDateTime createdAt;

    // Constructors
    public DepartmentDto() {
    }

    public DepartmentDto(int id, Integer makerId, Integer checkerId, int projectId, LocalDateTime createdAt) {
        this.id = id;
        this.makerId = makerId;
        this.checkerId = checkerId;
        this.projectId = projectId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getMakerId() {
        return makerId;
    }

    public void setMakerId(Integer makerId) {
        this.makerId = makerId;
    }

    public Integer getCheckerId() {
        return checkerId;
    }

    public void setCheckerId(Integer checkerId) {
        this.checkerId = checkerId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "DepartmentDto{" +
                "id=" + id +
                ", makerId=" + makerId +
                ", checkerId=" + checkerId +
                ", projectId=" + projectId +
                ", createdAt=" + createdAt +
                '}';
    }
}
