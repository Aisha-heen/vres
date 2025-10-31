package com.vres.service;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.Subscription;
 
@Service
public class SnsService {
 
    @Autowired
    private SnsClient snsClient;
 
    // The Topic ARN is now loaded from application properties (e.g., application.yml or application.properties)
    @Value("${vres.sns.topic-arn}")
    private String topicArn;
 
    /**
     * Subscribes an email address to the SNS topic.
     * AWS will automatically send a confirmation email.
     */
    public void subscribeEmail(String email) {
        final SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("email")
                .endpoint(email)
                .build();
        SubscribeResponse sns = snsClient.subscribe(subscribeRequest);
        System.out.println(sns.responseMetadata());
    }
 
    /**
     * Subscribes a phone number (SMS) to the SNS topic.
     * The phone number must be in E.164 format (e.g., +11234567890).
     */
    public void subscribeSms(String phoneNumber) {
        final SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("sms") // Protocol for SMS
                .endpoint(phoneNumber)
                .build();
        snsClient.subscribe(subscribeRequest);
    }
 
    /**
     * Publishes a message to the SNS topic, which is sent to all confirmed
     * subscribers (email and SMS).
     * @param message The content of the notification.
     * @param subject The subject line (primarily for email).
     * @return The message ID of the published notification.
     */
    public String publish(String message, String subject) {
        final PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .subject(subject)
                .build();
 
        PublishResponse result = snsClient.publish(publishRequest);
        return result.messageId();
    }
 
 
    /**
     * Checks if a subscription for a given endpoint (email or phone number) is confirmed.
     * @param endpoint The email address or phone number (E.164) to check.
     * @return true if confirmed, false if pending or not found.
     */
    public boolean isSubscriptionConfirmed(String endpoint) {
        try {
            ListSubscriptionsByTopicRequest request = ListSubscriptionsByTopicRequest.builder()
                    .topicArn(topicArn)
                    .build();
 
            ListSubscriptionsByTopicResponse response = snsClient.listSubscriptionsByTopic(request);
 
            for (Subscription subscription : response.subscriptions()) {
                if (subscription.endpoint().equalsIgnoreCase(endpoint)) {
                    // If the ARN is not "PendingConfirmation", it means the user has confirmed.
                    // This is robust for both email (requires link click) and SMS (often auto-confirmed).
                    return !"PendingConfirmation".equalsIgnoreCase(subscription.subscriptionArn());
                }
            }
        } catch (SnsException e) {
            // Log the error for internal investigation
            System.err.println("Error checking SNS subscription for endpoint " + endpoint + ": " + e.getMessage());
            // Re-throw or handle as appropriate for your application
            throw e;
        }
        // Return false if no matching subscription is found or an error occurs.
        return false;
    }
    
    public String publishSmsDirect(String phoneNumber, String message) {
        // NOTE: No topic ARN is used here.
        final PublishRequest publishRequest = PublishRequest.builder()
                .phoneNumber(phoneNumber) // Publish directly to the phone number
                .message(message)
                .build();
 
        PublishResponse result = snsClient.publish(publishRequest);
        return result.messageId();
    }
}