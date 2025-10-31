package com.vres.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date; // Keep using java.sql.Date as passed from ProjectService
import java.time.Duration;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Jackson library for creating JSON payload
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class BrevoSmsService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sms.sender}")
    private String smsSenderName;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper; // Use Jackson for JSON

    public BrevoSmsService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sends an SMS notification when a voucher is created using the specified format.
     *
     * @param recipientPhoneNumber The beneficiary's phone number (E.164 format).
     * @param voucherCode          The unique string code of the voucher.
     * @param qrCodeUrl            The URL link to the QR code image.
     * @param validityStart        The voucher validity start date.
     * @param validityEnd          The voucher validity end date.
     * @param voucherPoints        The points value of the voucher.
     * @param projectName          The name of the project. // <-- NEW PARAMETER
     */
    public void sendVoucherSms(String recipientPhoneNumber, String voucherCode, String qrCodeUrl,
                               Date validityStart, Date validityEnd, double voucherPoints,
                               String projectName) { // <-- NEW PARAMETER

        String brevoApiUrl = "https://api.brevo.com/v3/transactionalSMS/send";

        // Validate essential inputs
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
             System.err.println("❌ Brevo API Key is missing or empty in configuration. Cannot send SMS.");
             return;
        }
         if (smsSenderName == null || smsSenderName.isBlank()) {
             System.err.println("❌ Brevo SMS Sender Name is missing or empty in configuration. Cannot send SMS.");
             return;
        }
        if (recipientPhoneNumber == null || !recipientPhoneNumber.startsWith("+")) {
            System.err.println("❌ Invalid recipient phone number format: " + recipientPhoneNumber + ". Must start with '+'. Skipping SMS.");
            return;
        }

        // Format dates
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        String startDateFormatted = validityStart.toLocalDate().format(dateFormatter);
        String endDateFormatted = validityEnd.toLocalDate().format(dateFormatter);

        // --- UPDATED SMS CONTENT FORMAT ---
        String smsContent = String.format(
            "Hi, You have been issued a voucher worth Points: %.0f under project %s.\nValid: %s to %s.\nTo redeem, use Voucher Code: %s OR\nQR Link: %s",
            voucherPoints,
            projectName, // Use the project name
            startDateFormatted,
            endDateFormatted,
            voucherCode,
            qrCodeUrl
        );
        // --- END UPDATED FORMAT ---

        try {
            // --- USE Jackson ObjectMapper TO BUILD JSON ---
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("sender", smsSenderName);
            payload.put("recipient", recipientPhoneNumber);
            payload.put("content", smsContent);
            // payload.put("type", "transactional"); // Optional: set type if needed
            // payload.put("tag", "VoucherIssuance"); // Optional: set tag if needed
            String jsonPayload = objectMapper.writeValueAsString(payload);
            // --- END JSON BUILDING ---

            // Build HTTP Request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(brevoApiUrl))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("api-key", brevoApiKey)
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Log before sending
            System.out.println("Attempting to send Brevo SMS...");
            System.out.println("  Recipient: " + recipientPhoneNumber);
            System.out.println("  Sender: " + smsSenderName);
            System.out.println("  Payload: " + jsonPayload); // Log the actual payload being sent

            // Send Request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String body = response.body();

            // Log after sending
            System.out.println("Brevo API Response Status: " + status);
            System.out.println("Brevo API Response Body: " + body);

            if (status == 201) {
                System.out.println("✅ Brevo SMS sent successfully (API acknowledged) to " + recipientPhoneNumber);
            } else {
                System.err.println("❌ Failed to send Brevo SMS to " + recipientPhoneNumber + ". Brevo API returned HTTP " + status);
                System.err.println("Brevo Response Body: " + body);
            }

        } catch (Exception e) {
            System.err.println("❌ Exception occurred while preparing or sending Brevo SMS to " + recipientPhoneNumber);
            e.printStackTrace();
        }
    }
}