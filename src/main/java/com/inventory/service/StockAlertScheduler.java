package com.inventory.service;

import com.inventory.model.DBConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Scheduled background job with intelligent 24-hour rate limiting for low-stock email alerts.
 *
 * Technical Interview Talking Points:
 * 1. Debouncing & Rate Limiting (Cooldown Pattern):
 *    - When a product's stock drops low (<= min_required), an alert is sent ONCE immediately.
 *    - To prevent inbox flooding every scheduled minute, subsequent audits check elapsed time.
 *    - If the wholesaler has not restocked after 24 hours (1 full day), a follow-up reminder is sent.
 * 2. Self-Healing State Reset:
 *    - Once a product's stock is replenished (> min_required), its timestamp record is cleared.
 *    - If stock drops low again in the future, an initial alert will trigger immediately.
 * 3. Thread-Safe Concurrency:
 *    - Uses ConcurrentHashMap to manage timestamps across scheduled threads and cashier checkout events.
 */
@Component
public class StockAlertScheduler {

    private final EmailAlertService emailAlertService;

    // Track when an alert was last sent for each product (productId -> epoch ms)
    private static final Map<Integer, Long> lastAlertTimestamps = new ConcurrentHashMap<>();

    // 24 hours in milliseconds (1 day = 86,400,000 ms). Configurable in properties.
    @Value("${app.stock-alert.resend-interval-ms:86400000}")
    private long resendIntervalMs;

    public StockAlertScheduler(EmailAlertService emailAlertService) {
        this.emailAlertService = emailAlertService;
    }

    /**
     * Periodic background audit for stock levels and automated 1-day reminders.
     */
    @Scheduled(fixedRateString = "${app.stock-alert.fixed-rate:60000}")
    public void checkLowStockAndNotify() {
        System.out.println("[SCHEDULER] Running low-stock audit with 24-hr reminder check...");

        // Query active products
        String sql = "SELECT id, name, quantity, min_required FROM products WHERE is_active = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int initialSentCount = 0;
            int reminderSentCount = 0;
            int throttledCount = 0;
            long now = System.currentTimeMillis();

            while (rs.next()) {
                int productId = rs.getInt("id");
                String productName = rs.getString("name");
                int currentQuantity = rs.getInt("quantity");
                int minRequired = rs.getInt("min_required");

                if (currentQuantity <= minRequired) {
                    Long lastSentTime = lastAlertTimestamps.get(productId);

                    if (lastSentTime == null) {
                        // CASE 1: Stock just went low! Send initial email alert immediately.
                        System.out.println(String.format(
                            "[FIRST LOW-STOCK ALERT] Product '%s' (ID %d) dropped to low stock (Stock: %d, Min: %d). Sending alert email...",
                            productName, productId, currentQuantity, minRequired
                        ));
                        emailAlertService.sendLowStockEmail(productName, currentQuantity, minRequired, false);
                        lastAlertTimestamps.put(productId, now);
                        initialSentCount++;

                    } else {
                        long elapsed = now - lastSentTime;

                        if (elapsed >= resendIntervalMs) {
                            // CASE 2: Stock has remained low for 24+ hours without restock. Resend 1-day reminder!
                            System.out.println(String.format(
                                "[24-HR REMINDER TRIGGERED] Wholesaler has not restocked '%s' (ID %d) after 1 day. Resending reminder email...",
                                productName, productId
                            ));
                            emailAlertService.sendLowStockEmail(productName, currentQuantity, minRequired, true);
                            lastAlertTimestamps.put(productId, now);
                            reminderSentCount++;

                        } else {
                            // CASE 3: Already alerted recently (< 1 day ago). Suppress duplicate emails!
                            throttledCount++;
                            long remainingHours = (resendIntervalMs - elapsed) / (1000 * 60 * 60);
                            long remainingMinutes = ((resendIntervalMs - elapsed) / (1000 * 60)) % 60;
                            System.out.println(String.format(
                                "[ALERT SUPPRESSED] Product '%s' is still low, but email was sent recently. Next 1-day reminder in ~%dh %dm.",
                                productName, remainingHours, remainingMinutes
                            ));
                        }
                    }
                } else {
                    // CASE 4: Product is healthy (stock > min_required).
                    // If it was previously tracked as low stock, reset its tracker!
                    if (lastAlertTimestamps.remove(productId) != null) {
                        System.out.println(String.format(
                            "[STOCK REPLENISHED] Product '%s' (ID %d) has been restocked (Stock: %d > Min: %d). Alert tracker reset.",
                            productName, productId, currentQuantity, minRequired
                        ));
                    }
                }
            }

            System.out.println(String.format(
                "[SCHEDULER COMPLETE] Initial alerts sent: %d | 24-hr reminders sent: %d | Throttled (suppressed): %d",
                initialSentCount, reminderSentCount, throttledCount
            ));

        } catch (SQLException e) {
            System.err.println("[SCHEDULER ERROR] Database failure during low-stock query: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[SCHEDULER ERROR] Unexpected failure during stock alert job: " + e.getMessage());
        }
    }

    /**
     * Immediately checks and alerts when a purchase transaction reduces stock to or below threshold.
     */
    public void triggerImmediateAlertIfDue(int productId, String productName, int currentStock, int minRequired) {
        if (currentStock <= minRequired) {
            Long lastSent = lastAlertTimestamps.get(productId);
            long now = System.currentTimeMillis();

            if (lastSent == null) {
                System.out.println(String.format(
                    "[REAL-TIME SALE ALERT] Product '%s' (ID %d) reduced to low stock (Stock: %d, Min: %d). Sending alert...",
                    productName, productId, currentStock, minRequired
                ));
                emailAlertService.sendLowStockEmail(productName, currentStock, minRequired, false);
                lastAlertTimestamps.put(productId, now);
            }
        }
    }

    /**
     * Resets the alert cooldown tracker when an item is restocked.
     */
    public void resetAlertForProduct(int productId) {
        lastAlertTimestamps.remove(productId);
    }

    public static void clearAlertCooldown(int productId) {
        lastAlertTimestamps.remove(productId);
    }
}
