package com.vres.service;

import java.net.URI;
import java.net.http.HttpClient; // Import HttpClient
import java.net.http.HttpRequest; // Import HttpRequest
import java.net.http.HttpResponse; // Import HttpResponse
import java.time.Duration; // Import Duration
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // Import @Value
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vres.entity.Projects;
import com.vres.entity.Users;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    // --- Injected properties from application.properties ---
    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.email.sender.email}")
    private String senderEmail;

    @Value("${brevo.email.sender.name}")
    private String senderName;

    // --- Add HttpClient and ObjectMapper ---
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Constructor to initialize the HTTP client and JSON mapper
    public EmailService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * This is the real implementation that sends the email via Brevo.
     */
    private void sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        String brevoApiUrl = "https://api.brevo.com/v3/smtp/email";

        // Validate essential inputs
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            logger.error("❌ Brevo API Key is missing. Cannot send email to {}.", toEmail);
            return; // Stop execution if API key is missing
        }
         if (senderEmail == null || senderEmail.isBlank()) {
            logger.error("❌ Brevo Sender Email is missing. Cannot send email to {}.", toEmail);
            return; // Stop execution if sender email is missing
        }

        try {
            // Create Sender
            ObjectNode senderNode = objectMapper.createObjectNode();
            senderNode.put("name", senderName);
            senderNode.put("email", senderEmail);

            // Create Recipient
            ObjectNode recipientNode = objectMapper.createObjectNode();
            recipientNode.put("name", toName);
            recipientNode.put("email", toEmail);

            // Create 'to' array and add recipient
            ArrayNode toArray = objectMapper.createArrayNode();
            toArray.add(recipientNode);

            // Create main payload
            ObjectNode payload = objectMapper.createObjectNode();
            payload.set("sender", senderNode);
            payload.set("to", toArray);
            payload.put("subject", subject);
            payload.put("htmlContent", htmlContent);
            // payload.put("textContent", "Please view this email in an HTML-compatible client."); // Optional

            String jsonPayload = objectMapper.writeValueAsString(payload);

            // Build HTTP Request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(brevoApiUrl))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("api-key", brevoApiKey)
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            logger.info("Attempting to send email via Brevo to: {}", toEmail);
            logger.debug("Brevo Email Payload: {}", jsonPayload);

            // Send Request Asynchronously (so it doesn't block the main thread)
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int status = response.statusCode();
                    String body = response.body();
                    // Brevo returns 201 Created on success
                    if (status == 201) { 
                        logger.info("✅ Brevo email sent successfully to {}. Response: {}", toEmail, body);
                    } else {
                        logger.error("❌ Failed to send Brevo email to {}. HTTP {}. Response: {}", toEmail, status, body);
                    }
                })
                .exceptionally(e -> {
                    logger.error("❌ Exception occurred while sending Brevo email to {}", toEmail, e);
                    return null;
                });

        } catch (Exception e) {
            // Catch errors from JSON creation
            logger.error("❌ Exception occurred while preparing Brevo email for {}", toEmail, e);
        }
    }

    // --- Email for Project Coordinator ---
    // (No changes to the methods below)
    public void sendCoordinatorAssignmentEmail(Users user, Projects project) {
        String subject = "You have been assigned as a Project Coordinator";
        String body = String.format(
            "<p>Hi %s,</p>" +
            "<p>You have been assigned as the Project Coordinator for the project: <strong>%s</strong> (ID: %d).</p>",
            user.getName(), project.getTitle(), project.getId()
        );
        sendEmail(user.getEmail(), user.getName(), subject, body);
    }

    // --- Email for Maker ---
    public void sendMakerAssignmentEmail(Users user, Projects project) {
        String subject = "You have been assigned as a Maker for project: " + project.getTitle();
        String endDate = project.getEnd_date() != null ? project.getEnd_date().toLocalDate().format(dateFormatter) : "N/A";
        
        String body = String.format(
            "<p>Hi %s,</p>" +
            "<p>You have been assigned as a Maker for the project: <strong>%s</strong> (ID: %d).</p>" +
            "<p>The registration end date for this project is <strong>%s</strong>. Please upload the relevant beneficiary list on or before this date.</p>",
            user.getName(), project.getTitle(), project.getId(), endDate
        );
        sendEmail(user.getEmail(), user.getName(), subject, body);
    }
    
    // --- Email for Checker (Triggered when registration ends) ---
    public void sendCheckerRegistrationEndedEmail(Users user, Projects project) {
        String subject = "Beneficiary Registration has ended for project: " + project.getTitle();
        String body = String.format(
            "<p>Hi %s,</p>" +
            "<p>This is to inform you that the beneficiary registration period for the project <strong>%s</strong> (ID: %d) has ended.</p>" +
            "<p>Please log in to the VRES portal to check and approve the relevant beneficiaries.</p>",
            user.getName(), project.getTitle(), project.getId()
        );
        sendEmail(user.getEmail(), user.getName(), subject, body);
    }
    
    // --- Email for Issuer (Triggered when approval is done) ---
    public void sendIssuerApprovalDoneEmail(Users user, Projects project) {
        String subject = "Beneficiary Approval Completed for project: " + project.getTitle();
        String body = String.format(
            "<p>Hi %s,</p>" +
            "<p>This is to inform you that the approval of beneficiaries for the project <strong>%s</strong> (ID: %d) is complete.</p>" +
            "<p>Please log in to the VRES portal to check and issue vouchers for the approved candidates.</p>",
            user.getName(), project.getTitle(), project.getId()
        );
        sendEmail(user.getEmail(), user.getName(), subject, body);
    }

    // --- Generic "You've been assigned" email for other roles (Checker, Issuer, Vendor) ---
    public void sendGenericAssignmentEmail(Users user, Projects project, String roleName) {
        String subject = String.format("You have been assigned as a %s for project: %s", roleName, project.getTitle());
        String body = String.format(
            "<p>Hi %s,</p>" +
            "<p>You have been assigned the role of <strong>%s</strong> for the project: <strong>%s</strong> (ID: %d).</p>",
            user.getName(), roleName, project.getTitle(), project.getId()
        );
        sendEmail(user.getEmail(), user.getName(), subject, body);
    }
}