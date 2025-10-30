//package com.inventory;
//
//import java.sql.*;
//import java.util.Scanner;
//
//public class CashierService {
//    public static void cashierMenu(Scanner scanner) {
//        while (true) {
//            System.out.println("\nCashier Menu");
//            System.out.println("1. Create Bill");
//            System.out.println("2. Exit");
//            System.out.print("Choose an option: ");
//            int choice = scanner.nextInt();
//            switch (choice) {
//                case 1 -> createBill(scanner);
//                case 2 -> { return; }
//                default -> System.out.println("Invalid choice.");
//            }
//        }
//    }
//
//    private static void createBill(Scanner scanner) {
//        try (Connection conn = DBConnection.getConnection()) {
//            System.out.print("Enter product ID: ");
//            int productId = scanner.nextInt();
//            System.out.print("Enter quantity sold: ");
//            int qtySold = scanner.nextInt();
//
//            PreparedStatement ps = conn.prepareStatement(
//                "UPDATE products SET quantity = quantity - ? WHERE id = ?");
//            ps.setInt(1, qtySold);
//            ps.setInt(2, productId);
//            int updated = ps.executeUpdate();
//
//            if (updated > 0) {
//                PreparedStatement billStmt = conn.prepareStatement(
//                    "INSERT INTO bills (product_id, quantity, timestamp) VALUES (?, ?, NOW())");
//                billStmt.setInt(1, productId);
//                billStmt.setInt(2, qtySold);
//                billStmt.executeUpdate();
//
//                PreparedStatement checkStock = conn.prepareStatement(
//                    "SELECT name, quantity, min_required FROM products WHERE id=?");
//                checkStock.setInt(1, productId);
//                ResultSet rs = checkStock.executeQuery();
//                if (rs.next() && rs.getInt("quantity") < rs.getInt("min_required")) {
//                    System.out.printf("ALERT: Product '%s' is low on stock! Remaining: %d%n",
//                            rs.getString("name"), rs.getInt("quantity"));
//                }
//                System.out.println("Bill created and inventory updated.");
//            } else {
//                System.out.println("Invalid product or quantity.");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

//package com.inventory;
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//
//public class CashierService {
//    public static void cashierMenu(Scanner scanner) {
//        while (true) {
//            System.out.println("\nCashier Menu");
//            System.out.println("1. Create Bill");
//            System.out.println("2. Check Bill");
//            System.out.println("3. Exit");
//            System.out.print("Choose an option: ");
//            int choice = scanner.nextInt();
//            switch (choice) {
//                case 1 -> createBill();
//                case 2 -> viewBills();
//                case 3 -> { return; }
//                default -> System.out.println("Invalid choice.");
//            }
//        }
//    }
//
//    public static void createBill() {
//        List<BillItem> billItems = new ArrayList<>();
//        double totalAmount = 0;
//        Scanner scanner = new Scanner(System.in);
//
//        while (true) {
//            System.out.print("Enter product ID (or 0 to finish): ");
//            int productId = scanner.nextInt();
//            if (productId == 0) break;
//
//            System.out.print("Enter quantity: ");
//            int quantity = scanner.nextInt();
//
//            try (Connection conn = DBConnection.getConnection()) {
//                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products WHERE id = ? AND is_active = TRUE");
//                stmt.setInt(1, productId);
//                ResultSet rs = stmt.executeQuery();
//
//                if (rs.next()) {
//                    String name = rs.getString("name");
//                    double price = rs.getDouble("price");
//                    int stock = rs.getInt("quantity");
//                    int minStock = rs.getInt("min_required");
//
//                    if (stock < quantity) {
//                        System.out.println("❌ Not enough stock for " + name + ". Available: " + stock);
//                        continue;
//                    }
//
//                    double subtotal = price * quantity;
//                    billItems.add(new BillItem(productId, name, quantity, price, subtotal));
//                    totalAmount += subtotal;
//
//                    PreparedStatement updateStmt = conn.prepareStatement(
//                            "UPDATE products SET quantity = quantity - ? WHERE id = ?");
//                    updateStmt.setInt(1, quantity);
//                    updateStmt.setInt(2, productId);
//                    updateStmt.executeUpdate();
//
//                    int remaining = stock - quantity;
//                    if (remaining < minStock) {
//                        System.out.println("⚠ Low stock alert: " + name + " (Remaining: " + remaining + ")");
//                    }
//
//                } else {
//                    System.out.println("❌ Product not found with ID: " + productId);
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        if (!billItems.isEmpty()) {
//            try (Connection conn = DBConnection.getConnection()) {
//                // Insert into bills table
//                PreparedStatement billStmt = conn.prepareStatement(
//                        "INSERT INTO bills (total_amount) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
//                billStmt.setDouble(1, totalAmount);
//                billStmt.executeUpdate();
//
//                // Get generated bill ID
//                ResultSet keys = billStmt.getGeneratedKeys();
//                int billId = -1;
//                if (keys.next()) {
//                    billId = keys.getInt(1);
//                }
//
//                // Insert bill items
//                for (BillItem item : billItems) {
//                    PreparedStatement itemStmt = conn.prepareStatement(
//                            "INSERT INTO bill_items (bill_id, product_id, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)");
//                    itemStmt.setInt(1, billId);
//                    itemStmt.setInt(2, item.id);
//                    itemStmt.setInt(3, item.quantity);
//                    itemStmt.setDouble(4, item.price);
//                    itemStmt.setDouble(5, item.subtotal);
//                    itemStmt.executeUpdate();
//                }
//
//                // Print receipt
//                System.out.println("\n------------ BILL RECEIPT ------------");
//                System.out.printf("Bill ID: %d | Date: %s%n", billId, new java.util.Date());
//                System.out.printf("%-15s %-5s %-8s %-10s%n", "Product", "Qty", "Price", "Total");
//                for (BillItem item : billItems) {
//                    System.out.printf("%-15s %-5d %-8.2f %-10.2f%n",
//                            item.name, item.quantity, item.price, item.subtotal);
//                }
//                System.out.println("--------------------------------------");
//                System.out.printf("Total Amount: %.2f%n", totalAmount);
//                System.out.println("✔ Bill saved and inventory updated.\n");
//
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println("❗ No products were added to the bill.");
//        }
//    }
//    static class BillItem {
//        int id;
//        String name;
//        int quantity;
//        double price;
//        double subtotal;
//
//        BillItem(int id, String name, int quantity, double price, double subtotal) {
//            this.id = id;
//            this.name = name;
//            this.quantity = quantity;
//            this.price = price;
//            this.subtotal = subtotal;
//        }
//    }
//
//    public static void viewBills() {
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("1. Search by Bill ID");
//        System.out.println("2. Search by Date (yyyy-MM-dd)");
//        System.out.print("Choose option: ");
//        int choice = scanner.nextInt();
//        scanner.nextLine(); // Consume newline
//
//        String query = "";
//        PreparedStatement stmt = null;
//
//        try (Connection conn = DBConnection.getConnection()) {
//            if (choice == 1) {
//                System.out.print("Enter Bill ID: ");
//                int billId = scanner.nextInt();
//                query = """
//                    SELECT b.bill_id, b.bill_date, bi.quantity, bi.price, bi.subtotal, p.name
//                    FROM bills b
//                    JOIN bill_items bi ON b.bill_id = bi.bill_id
//                    JOIN products p ON bi.product_id = p.id
//                    WHERE b.bill_id = ?
//                    """;
//                stmt = conn.prepareStatement(query);
//                stmt.setInt(1, billId);
//
//            } else if (choice == 2) {
//                System.out.print("Enter date (yyyy-MM-dd): ");
//                String date = scanner.nextLine();
//                query = """
//                    SELECT b.bill_id, b.bill_date, bi.quantity, bi.price, bi.subtotal, p.name
//                    FROM bills b
//                    JOIN bill_items bi ON b.bill_id = bi.bill_id
//                    JOIN products p ON bi.product_id = p.id
//                    WHERE DATE(b.bill_date) = ?
//                    ORDER BY b.bill_id
//                    """;
//                stmt = conn.prepareStatement(query);
//                stmt.setString(1, date);
//            } else {
//                System.out.println("❌ Invalid option.");
//                return;
//            }
//
//            ResultSet rs = stmt.executeQuery();
//
//            int currentBillId = -1;
//            double total = 0;
//
//            while (rs.next()) {
//                int billId = rs.getInt("bill_id");
//                String billDate = rs.getString("bill_date");
//                String productName = rs.getString("name");
//                int qty = rs.getInt("quantity");
//                double price = rs.getDouble("price");
//                double subtotal = rs.getDouble("subtotal");
//
//                if (billId != currentBillId) {
//                    if (currentBillId != -1) {
//                        System.out.println("Total Amount: ₹" + total);
//                        System.out.println("-----------------------------");
//                        total = 0;
//                    }
//                    System.out.println("Bill ID: " + billId + " | Date: " + billDate);
//                    System.out.println("Product\tQty\tPrice\tSubtotal");
//                    currentBillId = billId;
//                }
//
//                System.out.printf("%s\t%d\t%.2f\t%.2f%n", productName, qty, price, subtotal);
//                total += subtotal;
//            }
//
//            if (currentBillId != -1) {
//                System.out.println("Total Amount: ₹" + total);
//                System.out.println("-----------------------------");
//            } else {
//                System.out.println("❌ No bills found.");
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//
//}

















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
 * Uses @Transactional to ensure stock updates and bill recording are atomic.
 */
@Service
@RequiredArgsConstructor
public class CashierService {

    // Note: DBConnection uses static methods, so we don't need explicit autowiring
    // but the @Service and @Transactional annotations are essential for Spring.

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
            // Start transaction manually if not using Spring's JPA/Hibernate transaction manager
            // Although @Transactional handles it, explicit rollback/commit logic is safer with static DBConnection.
            conn.setAutoCommit(false);

            // 1. Pre-check stock for all items
            for (BillItemDto item : billDto.getItems()) {
                checkStockAndPrice(conn, item);
                calculatedTotal += item.getSubtotal();
            }

            // Verify the calculated total matches the DTO's total (for security/accuracy)
            if (Math.abs(calculatedTotal - billDto.getTotalAmount()) > 0.01) {
                // This is a crucial security check to prevent front-end tampering
                throw new IllegalStateException("Calculated total does not match bill total. Transaction aborted.");
            }

            // 2. Insert into bills table (Header)
            Integer billId = insertBillHeader(conn, calculatedTotal);
            billDto.setBillId(billId);

            // 3. Insert into bill_items table and update product quantity (Details)
            for (BillItemDto item : billDto.getItems()) {
                insertBillItem(conn, billId, item);
                updateProductStock(conn, item);
            }

            // 4. Commit all changes
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
            // Rollback on validation failure
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            throw new RuntimeException(e.getMessage(), e);
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

    private void checkStockAndPrice(Connection conn, BillItemDto item) throws SQLException {
        // Retrieve current stock, price, and min required quantity
        String sql = "SELECT price, quantity, min_required FROM products WHERE id = ? AND is_active = TRUE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getProductId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Product ID " + item.getProductId() + " not found or is inactive.");
                }

                double currentPrice = rs.getDouble("price");
                int currentStock = rs.getInt("quantity");

                if (currentStock < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for product " + item.getProductId() + ". Available: " + currentStock);
                }

                // Calculate subtotal based on database price (security check)
                double subtotal = currentPrice * item.getQuantity();

                // Set validated values back to the DTO
                item.setPrice(currentPrice);
                item.setSubtotal(subtotal);
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
                    // Note: We don't have a field for product name in BillItemDto,
                    // but we can retrieve product data if needed for display.
                    // For now, we only store the transaction details.
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