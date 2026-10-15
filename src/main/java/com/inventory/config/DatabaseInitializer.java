package com.inventory.config;

import com.inventory.model.DBConnection;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Automatically initializes and seeds the database schema upon deployment.
 *
 * Technical Interview Talking Points:
 * 1. Cloud Resilience (Render / Aiven / Cloud MySQL):
 *    - When deploying to Render or connecting to a newly provisioned Cloud MySQL database,
 *      tables and default users do not exist yet.
 *    - This CommandLineRunner executes on startup, idempotently creating tables (IF NOT EXISTS)
 *      and ensuring admin/cashier credentials exist.
 * 2. Raw JDBC Implementation:
 *    - Uses standard Statement and PreparedStatement via DBConnection.getConnection(),
 *      maintaining raw JDBC architectural consistency without Hibernate/JPA.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("[DB INITIALIZER] Verifying database schema and initial seed data...");

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Create Users Table
            String sqlUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role ENUM('admin', 'cashier') NOT NULL
                );
            """;

            // 2. Create Products Table
            String sqlProducts = """
                CREATE TABLE IF NOT EXISTS products (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    price DOUBLE NOT NULL,
                    quantity INT NOT NULL,
                    min_required INT NOT NULL,
                    is_active TINYINT(1) NOT NULL DEFAULT 1
                );
            """;

            // 3. Create Bills Table
            String sqlBills = """
                CREATE TABLE IF NOT EXISTS bills (
                    bill_id INT AUTO_INCREMENT PRIMARY KEY,
                    bill_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                    total_amount DOUBLE NOT NULL
                );
            """;

            // 4. Create Bill Items Table
            String sqlBillItems = """
                CREATE TABLE IF NOT EXISTS bill_items (
                    item_id INT AUTO_INCREMENT PRIMARY KEY,
                    bill_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT NOT NULL,
                    price DOUBLE NOT NULL,
                    subtotal DOUBLE NOT NULL,
                    FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
                    FOREIGN KEY (product_id) REFERENCES products(id)
                );
            """;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sqlUsers);
                stmt.execute(sqlProducts);
                stmt.execute(sqlBills);
                stmt.execute(sqlBillItems);
            }

            // 5. Ensure Default Admin and Cashier Users Exist
            ensureUser(conn, "admin", "admin123", "admin");
            ensureUser(conn, "cashier", "cashier123", "cashier");
            ensureUser(conn, "cashier1", "cash123", "cashier");

            // 6. Seed Sample Inventory if Products table is empty
            seedInitialProductsIfEmpty(conn);

            System.out.println("[DB INITIALIZER] Database schema verified and ready for cloud deployment.");

        } catch (Exception e) {
            System.err.println("[DB INITIALIZER WARNING] Could not complete database auto-initialization: " + e.getMessage());
        }
    }

    private void ensureUser(Connection conn, String username, String password, String role) {
        String checkSql = "SELECT id FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, password);
                        insertStmt.setString(3, role);
                        insertStmt.executeUpdate();
                        System.out.println("[DB INITIALIZER] Created default user: " + username + " (" + role + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DB INITIALIZER] Error checking/seeding user " + username + ": " + e.getMessage());
        }
    }

    private void seedInitialProductsIfEmpty(Connection conn) {
        String countSql = "SELECT COUNT(*) FROM products";
        String insertProductSql = "INSERT INTO products (name, price, quantity, min_required, is_active) VALUES (?, ?, ?, ?, 1)";

        try (Statement countStmt = conn.createStatement();
             ResultSet rs = countStmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("[DB INITIALIZER] Seeding initial inventory products catalog...");
                Object[][] sampleProducts = new Object[][] {
                    { "Maggi 2-Minute Noodles", 24.00, 35, 10 },
                    { "Potato Chips 150g", 30.00, 18, 5 },
                    { "Coca Cola 500ml", 40.00, 60, 15 },
                    { "Organic Honey 500g", 250.00, 22, 5 },
                    { "Dairy Milk Silk", 85.00, 40, 10 },
                    { "Tata Tea Gold 500g", 320.00, 15, 5 },
                    { "Sugar 1kg", 48.00, 2, 5 } // Sample low-stock item
                };

                for (Object[] prod : sampleProducts) {
                    try (PreparedStatement pStmt = conn.prepareStatement(insertProductSql)) {
                        pStmt.setString(1, (String) prod[0]);
                        pStmt.setDouble(2, (Double) prod[1]);
                        pStmt.setInt(3, (Integer) prod[2]);
                        pStmt.setInt(4, (Integer) prod[3]);
                        pStmt.executeUpdate();
                    }
                }
                System.out.println("[DB INITIALIZER] Seeded " + sampleProducts.length + " default inventory items.");
            }
        } catch (Exception e) {
            System.err.println("[DB INITIALIZER] Error seeding products: " + e.getMessage());
        }
    }
}
