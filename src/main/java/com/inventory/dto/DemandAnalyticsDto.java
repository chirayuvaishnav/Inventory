package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object for Seasonal Demand & Restock Forecasting.
 *
 * Technical Interview Talking Points:
 * 1. Historical Pattern Recognition: Aggregates checkout data from 'bills' and 'bill_items'
 *    for a given calendar month across previous years to discover recurring buying patterns.
 * 2. Predictive Restock Recommendation: Compares current warehouse stock against historical
 *    sales velocity to compute an automated recommendation ("CRITICAL", "MODERATE", "OPTIMAL").
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandAnalyticsDto implements Serializable {

    private int productId;
    private String productName;
    private int totalQuantitySold;
    private double totalRevenueGenerated;
    private int currentStock;
    private int minRequired;
    private String restockRecommendation;

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getTotalQuantitySold() { return totalQuantitySold; }
    public void setTotalQuantitySold(int totalQuantitySold) { this.totalQuantitySold = totalQuantitySold; }

    public double getTotalRevenueGenerated() { return totalRevenueGenerated; }
    public void setTotalRevenueGenerated(double totalRevenueGenerated) { this.totalRevenueGenerated = totalRevenueGenerated; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getMinRequired() { return minRequired; }
    public void setMinRequired(int minRequired) { this.minRequired = minRequired; }

    public String getRestockRecommendation() { return restockRecommendation; }
    public void setRestockRecommendation(String restockRecommendation) { this.restockRecommendation = restockRecommendation; }
}
