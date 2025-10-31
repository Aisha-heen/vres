package com.vres.dto;

import java.time.LocalDateTime;

public class AuditLogDto {

    private int id;
    private int actorUserId;
    private String action;
    private String entity;
    private int entityId;
    private String meta; // Using String for JSON meta
    private String ip;
    private String ua;
    private LocalDateTime createdAt;

    // Constructors
    public AuditLogDto() {
    }

    public AuditLogDto(int id, int actorUserId, String action, String entity, int entityId, String meta, String ip, String ua, LocalDateTime createdAt) {
        this.id = id;
        this.actorUserId = actorUserId;
        this.action = action;
        this.entity = entity;
        this.entityId = entityId;
        this.meta = meta;
        this.ip = ip;
        this.ua = ua;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(int actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AuditLogDto{" +
                "id=" + id +
                ", actorUserId=" + actorUserId +
                ", action='" + action + '\'' +
                ", entity='" + entity + '\'' +
                ", entityId=" + entityId +
                ", meta='" + meta + '\'' +
                ", ip='" + ip + '\'' +
                ", ua='" + ua + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
	