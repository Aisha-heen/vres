package com.vres.service;

import java.sql.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vres.entity.Notifications;
import com.vres.repository.NotificationsRepository;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationsRepository notificationsRepository;

    public void createNotification(String channel, String recipient, String templateKey, String payload) {
        logger.info("Creating notification for recipient: {} via channel: {}", recipient, channel);

        Notifications notification = new Notifications();
        notification.setChannel(channel);
        notification.setRecipient(recipient);
        notification.setTemplate_key(templateKey);
        notification.setPayload(payload);
        notification.setSent_at(new Date(System.currentTimeMillis())); // Set current time

        notificationsRepository.save(notification);

        logger.info("Notification successfully sent to {} via {}", recipient, channel);
    }
}