package com.vres.controller;
 
import com.vres.service.SnsService; // Import the service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sns.model.SnsException;
 
@RestController
@RequestMapping("/sns")
public class SNSController {
 
    @Autowired
    private SnsService snsService; // Use the service for all logic
 
    // NOTE: TOPIC_ARN moved to SnsService and application properties.
 
    /**
     * Subscribes an email address to the AWS SNS topic.
     * Endpoint: GET /sns/subscribe/email/{email}
     */
    @GetMapping("/subscribe/email/{email}")
    public ResponseEntity<String> subscribeEmailToTopic(@PathVariable String email) {
        try {
            snsService.subscribeEmail(email);
            return new ResponseEntity<>("Email subscription initiated. Please check your inbox to confirm.", HttpStatus.OK);
        } catch (SnsException e) {
            System.err.println("SNS Subscription error for email: " + e.getMessage());
            return new ResponseEntity<>("Failed to subscribe email. Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
 
    /**
     * Subscribes a phone number (SMS) to the AWS SNS topic.
     * This is for production. For testing, use the /send/sms endpoint below.
     * Endpoint: GET /sns/subscribe/sms/{phoneNumber}
     */
    @GetMapping("/subscribe/sms/{phoneNumber}")
    public ResponseEntity<String> subscribePhoneToTopic(@PathVariable String phoneNumber) {
        try {
            snsService.subscribeSms(phoneNumber);
            // SMS subscriptions are automatically confirmed or confirmed via a reply SMS.
            return new ResponseEntity<>("SMS subscription initiated for: " + phoneNumber + ". If in Sandbox, use /send/sms to test.", HttpStatus.OK);
        } catch (SnsException e) {
            System.err.println("SNS Subscription error for phone number: " + e.getMessage());
            return new ResponseEntity<>("Failed to subscribe phone number. Ensure E.164 format (+11234567890). Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
 
    /**
     * Publishes a message to the SNS topic (sends to ALL confirmed email and SMS subscribers).
     * Endpoint: GET /sns/publish/{msg}
     */
    @GetMapping("/publish/{msg}")
    public String publishToTopic(@PathVariable String msg) {
        try {
            String messageId = snsService.publish(msg, "VRES Notification");
            return "Message published to Topic! Message ID: " + messageId;
        } catch (SnsException e) {
            return "Failed to publish to topic. Error: " + e.getMessage();
        }
    }
 
    // --- New Endpoint for Sandbox Testing (Direct SMS) ---
 
    /**
     * Publishes a message directly to a single phone number (SMS).
     * This is the necessary method for testing in the SNS Sandbox.
     * Endpoint: GET /sns/send/sms/{phoneNumber}/{message}
     */
    @GetMapping("/send/sms/{phoneNumber}/{message}")
    public ResponseEntity<String> sendSmsDirect(
            @PathVariable String phoneNumber,
            @PathVariable String message) {
        try {
            String messageId = snsService.publishSmsDirect(phoneNumber, message);
            return new ResponseEntity<>("Direct SMS published to " + phoneNumber + ". Message ID: " + messageId, HttpStatus.OK);
        } catch (SnsException e) {
            System.err.println("SNS Direct Publish error: " + e.getMessage());
            return new ResponseEntity<>(
                "Failed to send direct SMS. Ensure: 1) E.164 format. 2) Number is **VERIFIED** in the AWS SMS Sandbox. Error: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
 
    /**
     * Verifies the subscription status for a given endpoint (email or phone number).
     * Endpoint: GET /sns/verify/{endpoint}
     */
    @GetMapping("/verify/{endpoint}")
    public ResponseEntity<String> verifySubscription(@PathVariable String endpoint) {
        try {
            boolean isConfirmed = snsService.isSubscriptionConfirmed(endpoint);
            String status = isConfirmed ? "Confirmed" : "Pending Confirmation (Check Inbox/SMS) or Not Found";
            return new ResponseEntity<>("Subscription status for " + endpoint + ": " + status, HttpStatus.OK);
        } catch (SnsException e) {
            return new ResponseEntity<>("Error checking subscription status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}