package com.Hontec.service;

import com.Hontec.model.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SendGridEmailService {

    @Value("${app.sendgrid.api-key}")
    private String apiKey;

    @Value("${app.sendgrid.from-email}")
    private String fromEmail;

    @Value("${app.manager.email}")
    private String managerEmail;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void sendDispenseNotification(Invoice invoice) {
        log.info("Preparing to send dispense notification email for Invoice ID: {}", invoice.getId());

        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("placeholder")) {
            log.warn("SendGrid API key is not configured or is a placeholder. Skipping email delivery.");
            return;
        }

        try {
            String customerName = invoice.getCustomer().getName();
            Long invoiceId = invoice.getId();

            String itemsList = invoice.getItems().stream()
                    .map(item -> String.format("<li>Product: %s, Batch: %s, Quantity: %d</li>",
                            item.getProduct().getName(),
                            item.getBatch().getBatchNo(),
                            item.getQty()))
                    .collect(Collectors.joining());

            String htmlContent = String.format(
                    "<h3>Invoice Dispensed Successfully</h3>" +
                    "<p><strong>Invoice ID:</strong> %d</p>" +
                    "<p><strong>Customer:</strong> %s</p>" +
                    "<p><strong>Dispensed Medicines:</strong></p>" +
                    "<ul>%s</ul>",
                    invoiceId, customerName, itemsList);

            // Constructing JSON manually to avoid jackson mapping issues in lightweight service
            String requestBody = "{"
                    + "\"personalizations\":[{"
                    + "\"to\":[{\"email\":\"" + managerEmail + "\"}]"
                    + "}],\"from\":{\"email\":\"" + fromEmail + "\"},"
                    + "\"subject\":\"Medicines Dispensed - Invoice #" + invoiceId + "\","
                    + "\"content\":[{"
                    + "\"type\":\"text/html\","
                    + "\"value\":\"" + escapeJson(htmlContent) + "\""
                    + "}]"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            log.info("Dispense email notification sent successfully via SendGrid HTTP API. Status: {}", response.statusCode());
                        } else {
                            log.error("Failed to send email via SendGrid HTTP API. Status: {}, Body: {}", response.statusCode(), response.body());
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Error occurred while executing SendGrid HTTP request", ex);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Failed to build or send email notification via SendGrid", e);
        }
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
