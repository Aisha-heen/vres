package com.vres.entity;

import java.sql.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="notifications")
public class Notifications {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private int id;
	@Column
	private String channel;
	@Column
	private String recipient;
	@Column
	private String template_key;
	@Column
	private String payload;
	@CreationTimestamp
	@Column
	private Date sent_at;
	
	public Notifications() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getTemplate_key() {
		return template_key;
	}

	public void setTemplate_key(String template_key) {
		this.template_key = template_key;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public Date getSent_at() {
		return sent_at;
	}

	public void setSent_at(Date sent_at) {
		this.sent_at = sent_at;
	}

	public Notifications(int id, String channel, String recipient, String template_key, String payload, Date sent_at) {
		super();
		this.id = id;
		this.channel = channel;
		this.recipient = recipient;
		this.template_key = template_key;
		this.payload = payload;
		this.sent_at = sent_at;
	}
}
