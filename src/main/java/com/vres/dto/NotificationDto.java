package com.vres.dto;

import java.time.LocalDateTime;

public class NotificationDto {

    private int id;
    private String channel;
    private String recipient;
    private String templateKey;
    private String payload;
    private LocalDateTime sentAt;

    // Constructors
    public NotificationDto() {
    }

    public NotificationDto(int id, String channel, String recipient, String templateKey, String payload, LocalDateTime sentAt) {
        this.id = id;
        this.channel = channel;
        this.recipient = recipient;
        this.templateKey = templateKey;
        this.payload = payload;
        this.sentAt = sentAt;
    }

    // Getters and Setters
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

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public String toString() {
        return "NotificationDto{" +
                "id=" + id +
                ", channel='" + channel + '\'' +
                ", recipient='" + recipient + '\'' +
                ", templateKey='" + templateKey + '\'' +
                ", payload='" + payload + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}
