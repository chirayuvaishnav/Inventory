package com.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling all cashier-related business logic, primarily sales and billing.
 * Uses @Transactional to ensure stock updates and bill recording are atomic (succeed or fail together).
 */
@Service
@RequiredArgsConstructor
public class CashierService {

    /**
     * Retrieves all active products. Used by the cashier frontend to populate the list.
     * @return List of ProductDto for active products.
     */
    public List<ProductDto> getActiveProducts() {
        List<ProductDto> products = new ArrayList<>();
        // Only fetch products that are active and have stock greater than 0
        String sql = "SELECT id, name, price, quantity, min_required FROM products WHERE is_active = TRUE AND quantity > 0 ORDER BY name ASC";

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
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Database error retrieving active products: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve active products.", e);
        }
        return products;
    }


    /**
     * Processes a new sale (bill). This method is transactional, ensuring
     * inventory updates and bill recording succeed or fail together.
     * @param billDto The DTO containing the list of items to be purchased.
     * @return The completed BillDto with the generated billId and total.
     */
    @Transactional
    public BillDto processSale(BillDto billDto) {
        if (billDto.getItems() == null || billDto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Bill must contain at least one item.");
        }

        double calculatedTotal = 0;
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Manually manage transaction

            // 1. Pre-check stock and price validation for all items
            for (BillItemDto item : billDto.getItems()) {
                checkStockAndPrice(conn, item);
                calculatedTotal += item.getSubtotal();
            }

            // 2. Final security check on the total amount
            if (Math.abs(calculatedTotal - billDto.getTotalAmount()) > 0.01) {
                throw new IllegalStateException("Calculated total ($" + calculatedTotal + ") does not match bill total ($" + billDto.getTotalAmount() + "). Transaction aborted.");
            }

            // 3. Insert into bills table (Header)
            Integer billId = insertBillHeader(conn, calculatedTotal);
            billDto.setBillId(billId);

            // 4. Insert into bill_items table and update product quantity (Details)
            for (BillItemDto item : billDto.getItems()) {
                insertBillItem(conn, billId, item);
                updateProductStock(conn, item);
            }

            // 5. Commit all changes
            conn.commit();

        } catch (SQLException e) {
            // Rollback on any SQL failure
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("Sale transaction failed due to database error: " + e.getMessage(), e);
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Rollback on validation failure (stock check, empty bill, etc.)
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            // CRITICAL FIX: Re-throw IllegalStateException directly so Controller can return 400 Bad Request
            throw e;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset autocommit
                    conn.close();
                }
            } catch (SQLException closeEx) {
                System.err.println("Connection close failed: " + closeEx.getMessage());
            }
        }
        return billDto;
    }

    /**
     * Checks if stock is available and validates the current price from the database.
     * Throws IllegalStateException if stock is insufficient or product is inactive.
     */
    private void checkStockAndPrice(Connection conn, BillItemDto item) throws SQLException {
        String sql = "SELECT price, quantity, min_required, name FROM products WHERE id = ? AND is_active = TRUE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getProductId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Product ID " + item.getProductId() + " not found or is inactive.");
                }

                double currentPrice = rs.getDouble("price");
                int currentStock = rs.getInt("quantity");
                String productName = rs.getString("name");

                if (currentStock < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for " + productName + ". Available: " + currentStock + ", Requested: " + item.getQuantity());
                }

                // Set validated values back to the DTO (used later for insertion)
                item.setPrice(currentPrice);
                item.setSubtotal(currentPrice * item.getQuantity());
            }
        }
    }

    private Integer insertBillHeader(Connection conn, double totalAmount) throws SQLException {
        String sql = "INSERT INTO bills (total_amount) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, totalAmount);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve generated bill ID.");
                }
            }
        }
    }

    private void insertBillItem(Connection conn, int billId, BillItemDto item) throws SQLException {
        String sql = "INSERT INTO bill_items (bill_id, product_id, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getPrice());
            stmt.setDouble(5, item.getSubtotal());
            stmt.executeUpdate();
        }
    }

    private void updateProductStock(Connection conn, BillItemDto item) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getQuantity());
            stmt.setInt(2, item.getProductId());
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves a single bill and its items.
     * @param billId The ID of the bill to retrieve.
     * @return The BillDto containing all header and line item details.
     */
    public BillDto getBillById(int billId) {
        BillDto billDto = new BillDto();
        List<BillItemDto> items = new ArrayList<>();
        // Query to join bill header and bill items, and product name
        String sql = """
            SELECT
                b.bill_id, b.bill_date, b.total_amount,
                bi.quantity, bi.price, bi.subtotal,
                p.id as product_id, p.name as product_name
            FROM bills b
            JOIN bill_items bi ON b.bill_id = bi.bill_id
            JOIN products p ON bi.product_id = p.id
            WHERE b.bill_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean firstRow = true;
                while (rs.next()) {
                    if (firstRow) {
                        // Populate header data from the first row
                        billDto.setBillId(rs.getInt("bill_id"));
                        billDto.setTotalAmount(rs.getDouble("total_amount"));
                        billDto.setBillDate(rs.getTimestamp("bill_date").toString());
                        firstRow = false;
                    }

                    // Populate item data
                    BillItemDto item = new BillItemDto();
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getDouble("price"));
                    item.setSubtotal(rs.getDouble("subtotal"));
                    items.add(item);
                }

                if (firstRow) {
                    throw new RuntimeException("Bill ID " + billId + " not found.");
                }
                billDto.setItems(items);

            }
        } catch (SQLException e) {
            System.err.println("Database error retrieving bill: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve bill details.", e);
        }
        return billDto;
    }
}