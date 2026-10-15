package com.inventory.service;

import com.inventory.model.DBConnection;
import com.inventory.dto.ProductDto;
import com.inventory.dto.DemandAnalyticsDto;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling all administrative product-related business logic.
 */
@Service
public class AdminService {

    /**
     * Retrieves all ACTIVE products from the database and maps them to DTOs.
     */
    public List<ProductDto> getAllProducts() {
        List<ProductDto> products = new ArrayList<>();
        // Only select active products for the dashboard view
        String sql = "SELECT id, name, price, quantity, min_required, is_active FROM products WHERE is_active = TRUE ORDER BY id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ProductDto product = new ProductDto();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setQuantity(rs.getInt("quantity"));
                product.setMinRequired(rs.getInt("min_required"));
                product.setIsActive(rs.getBoolean("is_active"));
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Database error retrieving products: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve products from database.", e);
        }
        return products;
    }

    /**
     * Adds a new product to the database.
     */
    public ProductDto addProduct(ProductDto productDto) {
        String sql = "INSERT INTO products (name, price, quantity, min_required, is_active) VALUES (?, ?, ?, ?, TRUE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, productDto.getName());
            stmt.setDouble(2, productDto.getPrice());
            stmt.setInt(3, productDto.getQuantity());
            stmt.setInt(4, productDto.getMinRequired());

            stmt.executeUpdate();

            // Retrieve the auto-generated ID
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                productDto.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Database error during product creation: " + e.getMessage());
            throw new RuntimeException("Failed to add product due to a database issue.", e);
        }
        return productDto;
    }

    public void deleteProduct(int id) {
        String sql = "UPDATE products SET is_active = FALSE WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                // If the product was not found (and thus not updated)
                throw new RuntimeException("Product ID " + id + " not found or already inactive.");
            }
        } catch (SQLException e) {
            System.err.println("Database error during product deactivation: " + e.getMessage());
            throw new RuntimeException("Failed to deactivate product due to a database issue.", e);
        }
    }
    public void updateQuantity(int productId, int quantityToAdd) {
        // SQL adds the quantityToAdd to the existing quantity
        String sql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantityToAdd);
            stmt.setInt(2, productId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                // If the product doesn't exist, throw a 404-like error
                throw new RuntimeException("Product ID " + productId + " not found.");
            }
            StockAlertScheduler.clearAlertCooldown(productId);
        } catch (SQLException e) {
            System.err.println("Database error during restock: " + e.getMessage());
            throw new RuntimeException("Failed to restock product.", e);
        }
    }

    private static String billIdColumn = null;
    private static String dateColumn = null;

    private synchronized void resolveBillColumns(Connection conn) {
        if (billIdColumn != null && dateColumn != null) return;
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "bills", null)) {
            boolean hasBillId = false;
            boolean hasBillDate = false;
            while (rs.next()) {
                String col = rs.getString("COLUMN_NAME");
                if ("bill_id".equalsIgnoreCase(col)) hasBillId = true;
                if ("bill_date".equalsIgnoreCase(col)) hasBillDate = true;
            }
            billIdColumn = hasBillId ? "bill_id" : "id";
            dateColumn = hasBillDate ? "bill_date" : "timestamp";
        } catch (SQLException e) {
            billIdColumn = "bill_id";
            dateColumn = "bill_date";
        }
    }

    /**
     * Aggregates historical sales by calendar month to forecast inventory demand.
     * Compares sold volume against current stock to output predictive restock flags.
     */
    public List<DemandAnalyticsDto> getMonthlyDemandAnalytics(int month, int year) {
        List<DemandAnalyticsDto> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            resolveBillColumns(conn);

            String yearCondition = (year > 0) ? " AND YEAR(b." + dateColumn + ") = ?" : "";
            String sql = "SELECT p.id, p.name, p.quantity, p.min_required, " +
                         "COALESCE(SUM(bi.quantity), 0) AS total_sold, " +
                         "COALESCE(SUM(bi.subtotal), 0) AS total_revenue " +
                         "FROM bill_items bi " +
                         "JOIN bills b ON bi.bill_id = b." + billIdColumn + " " +
                         "JOIN products p ON bi.product_id = p.id " +
                         "WHERE MONTH(b." + dateColumn + ") = ?" + yearCondition + " " +
                         "GROUP BY p.id, p.name, p.quantity, p.min_required " +
                         "ORDER BY total_sold DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, month);
                if (year > 0) {
                    stmt.setInt(2, year);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        DemandAnalyticsDto dto = new DemandAnalyticsDto();
                        dto.setProductId(rs.getInt("id"));
                        dto.setProductName(rs.getString("name"));
                        dto.setCurrentStock(rs.getInt("quantity"));
                        dto.setMinRequired(rs.getInt("min_required"));
                        dto.setTotalQuantitySold(rs.getInt("total_sold"));
                        dto.setTotalRevenueGenerated(rs.getDouble("total_revenue"));

                        // Restock recommendation logic
                        int sold = dto.getTotalQuantitySold();
                        int stock = dto.getCurrentStock();
                        if (stock < sold * 0.5) {
                            dto.setRestockRecommendation("CRITICAL: High seasonal demand predicted; pre-stock immediately.");
                        } else if (stock < sold) {
                            dto.setRestockRecommendation("MODERATE: Stock lower than previous monthly demand. Refill soon.");
                        } else {
                            dto.setRestockRecommendation("OPTIMAL: Sufficient inventory to meet historical demand.");
                        }

                        list.add(dto);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error retrieving monthly demand analytics: " + e.getMessage());
            throw new RuntimeException("Failed to calculate monthly demand analytics.", e);
        }
        return list;
    }

    /**
     * Computes high-level store intelligence summary and recent monthly revenue trends for charts.
     */
    public Map<String, Object> getOverallStoreStats() {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {
            resolveBillColumns(conn);

            // 1. Total lifetime revenue & orders count
            String billsSql = "SELECT COALESCE(SUM(total_amount), 0) AS total_revenue, COUNT(*) AS total_orders FROM bills";
            try (PreparedStatement stmt = conn.prepareStatement(billsSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalRevenue", rs.getDouble("total_revenue"));
                    stats.put("totalOrders", rs.getInt("total_orders"));
                }
            }

            // 2. Active products and low stock count
            String productsSql = "SELECT " +
                                 "COUNT(*) AS total_products, " +
                                 "SUM(CASE WHEN quantity <= min_required THEN 1 ELSE 0 END) AS low_stock_count " +
                                 "FROM products WHERE is_active = TRUE";
            try (PreparedStatement stmt = conn.prepareStatement(productsSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalProducts", rs.getInt("total_products"));
                    stats.put("lowStockCount", rs.getInt("low_stock_count"));
                }
            }

            // 3. Top 5 all-time bestselling products
            List<Map<String, Object>> topProducts = new ArrayList<>();
            String topSql = "SELECT p.id, p.name, COALESCE(SUM(bi.quantity), 0) AS total_sold, COALESCE(SUM(bi.subtotal), 0) AS total_revenue " +
                            "FROM bill_items bi " +
                            "JOIN products p ON bi.product_id = p.id " +
                            "GROUP BY p.id, p.name " +
                            "ORDER BY total_sold DESC LIMIT 5";
            try (PreparedStatement stmt = conn.prepareStatement(topSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("name", rs.getString("name"));
                    item.put("totalSold", rs.getInt("total_sold"));
                    item.put("revenue", rs.getDouble("total_revenue"));
                    topProducts.add(item);
                }
            }
            stats.put("topBestsellers", topProducts);

            // 4. Monthly revenue trends for the last 6 months (for Chart.js)
            List<Map<String, Object>> monthlyTrends = new ArrayList<>();
            String trendSql = "SELECT DATE_FORMAT(b." + dateColumn + ", '%b %Y') AS month_label, " +
                              "YEAR(b." + dateColumn + ") AS yr, MONTH(b." + dateColumn + ") AS mo, " +
                              "COALESCE(SUM(b.total_amount), 0) AS revenue, COUNT(*) AS orders " +
                              "FROM bills b " +
                              "WHERE b." + dateColumn + " IS NOT NULL " +
                              "GROUP BY yr, mo, month_label " +
                              "ORDER BY yr ASC, mo ASC LIMIT 12";
            try (PreparedStatement stmt = conn.prepareStatement(trendSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> trend = new LinkedHashMap<>();
                    trend.put("month", rs.getString("month_label"));
                    trend.put("revenue", rs.getDouble("revenue"));
                    trend.put("orders", rs.getInt("orders"));
                    monthlyTrends.add(trend);
                }
            }
            stats.put("monthlyTrends", monthlyTrends);

            // 5. Count of items with high seasonal demand (stock < sold)
            int highDemandCount = 0;
            java.time.LocalDate now = java.time.LocalDate.now();
            List<DemandAnalyticsDto> currentMonthAnalytics = getMonthlyDemandAnalytics(now.getMonthValue(), now.getYear());
            for (DemandAnalyticsDto d : currentMonthAnalytics) {
                if (d.getCurrentStock() < d.getTotalQuantitySold()) {
                    highDemandCount++;
                }
            }
            stats.put("highDemandCount", highDemandCount);

        } catch (SQLException e) {
            System.err.println("Database error calculating overall store stats: " + e.getMessage());
            throw new RuntimeException("Failed to calculate store stats.", e);
        }

        return stats;
    }
}