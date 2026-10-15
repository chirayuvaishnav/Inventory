package com.inventory.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service responsible for composing and sending low-stock email notifications.
 *
 * Technical Interview Talking Points:
 * 1. JavaMailSender: An interface provided by Spring's 'spring-boot-starter-mail'. Spring Boot
 *    auto-configures an implementation of this interface using the 'spring.mail.*' properties
 *    defined in application.properties or application-prod.properties.
 * 2. SimpleMailMessage: A simple model object for plain-text emails (supports To, From, Subject, Text).
 * 3. Graceful Error Handling: We catch MailException specifically so that transient email delivery
 *    failures (e.g., SMTP timeout or bad credentials) will NOT crash the scheduler or application.
 */
@Service
public class EmailAlertService {

    private final JavaMailSender mailSender;

    // Configured in application.properties / application-prod.properties
    @Value("${app.admin.alert-email}")
    private String adminAlertEmail;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailAlertService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an email alert or 24-hour follow-up reminder to the administrator/wholesaler.
     *
     * @param productName  The name of the flagged product
     * @param currentStock Current available quantity in the warehouse/store
     * @param minRequired  Minimum required threshold before restocking is needed
     * @param isReminder   True if this is a 24-hour follow-up reminder because stock remains un-replenished
     */
    public void sendLowStockEmail(String productName, int currentStock, int minRequired, boolean isReminder) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(adminAlertEmail);

            if (isReminder) {
                message.setSubject("[24-HR REMINDER] IMS Alert: Stock Still Low for " + productName);
                message.setText(String.format(
                    "Hello Wholesaler / Admin,\n\n" +
                    "This is an automated 24-hour follow-up reminder from your Inventory Management System (IMS).\n\n" +
                    "The stock for '%s' remains below safe operational levels and has NOT yet been restocked:\n\n" +
                    "  - Product Name: %s\n" +
                    "  - Current Warehouse Stock: %d unit(s)\n" +
                    "  - Minimum Required Threshold: %d unit(s)\n\n" +
                    "Action Required: Please place a purchase order or restock this item immediately to avoid inventory stockouts.\n\n" +
                    "Best regards,\n" +
                    "IMS Automated Notification Service",
                    productName, productName, currentStock, minRequired
                ));
            } else {
                message.setSubject("IMS Alert: Low Stock Warning for " + productName);
                message.setText(String.format(
                    "Hello Wholesaler / Admin,\n\n" +
                    "This is an automated notification from your Inventory Management System (IMS).\n\n" +
                    "The following product has just reached or fallen below its minimum required threshold:\n\n" +
                    "  - Product Name: %s\n" +
                    "  - Current Stock: %d unit(s)\n" +
                    "  - Minimum Required: %d unit(s)\n\n" +
                    "Action Required: Please arrange for restocking to prevent stockouts.\n\n" +
                    "Best regards,\n" +
                    "IMS Automated Notification Service",
                    productName, currentStock, minRequired
                ));
            }

            mailSender.send(message);
            String alertType = isReminder ? "24-hr reminder" : "initial low stock alert";
            System.out.println("[EMAIL SUCCESS] " + alertType + " sent for '" + productName + "' to " + adminAlertEmail);

        } catch (MailException e) {
            // Non-blocking error handling: logs failure without crashing the scheduler
            System.err.println("[EMAIL FAILED] Could not send alert for product '" + productName + "': " + e.getMessage());
        }
    }

    /**
     * Backward-compatible overload for initial alert.
     */
    public void sendLowStockEmail(String productName, int currentStock, int minRequired) {
        sendLowStockEmail(productName, currentStock, minRequired, false);
    }
}
