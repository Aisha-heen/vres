package com.vres.entity;

import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="audit_logs")
public class AuditLogs {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private int id;
	@Column
	private int actor_user_id;
	@Column
	private String action;
	@Column
	private String entity;
	@Column
	private int entity_id;
	@Column
	private String meta;
	@Column
	private String ip;
	@Column
	private String ua;
	@CreationTimestamp // This annotation automatically sets the timestamp when a user is created
    @Column(name = "created_at", nullable = false, updatable = false)
	private LocalDate created_at;
	
	public AuditLogs() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActor_user_id() {
		return actor_user_id;
	}

	public void setActor_user_id(int actor_user_id) {
		this.actor_user_id = actor_user_id;
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

	public int getEntity_id() {
		return entity_id;
	}

	public void setEntity_id(int entity_id) {
		this.entity_id = entity_id;
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

	public LocalDate getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDate date) {
		this.created_at = date;
	}

	public AuditLogs(int id, int actor_user_id, String action, String entity, int entity_id, String meta, String ip,
			String ua, LocalDate created_at) {
		super();
		this.id = id;
		this.actor_user_id = actor_user_id;
		this.action = action;
		this.entity = entity;
		this.entity_id = entity_id;
		this.meta = meta;
		this.ip = ip;
		this.ua = ua;
		this.created_at = created_at;
	}
}
