//package com.inventory;
//
//import java.sql.*;
//import java.util.Scanner;
//
//public class AdminService {
//    public static void adminMenu(Scanner scanner) {
//        while (true) {
//            System.out.println("\nAdmin Menu");
//            System.out.println("1. Add Product");
//            System.out.println("2. View Products");
//            System.out.println("3. Delete Product");
//            System.out.println("4. Exit");
//            System.out.print("Choose an option: ");
//            int choice = scanner.nextInt();
//            scanner.nextLine();
//            switch (choice) {
//                case 1 -> addProduct(scanner);
//                case 2 -> viewProducts();
//                case 3 -> deleteProduct(scanner);
//                case 4 -> { return; }
//                default -> System.out.println("Invalid choice.");
//            }
//        }
//    }
//
//    private static void addProduct(Scanner scanner) {
//        try (Connection conn = DBConnection.getConnection()) {
//            System.out.print("Enter product name: ");
//            String name = scanner.nextLine();
//            System.out.print("Enter quantity: ");
//            int qty = scanner.nextInt();
//            System.out.print("Enter minimum required quantity: ");
//            int minQty = scanner.nextInt();
//            String sql = "INSERT INTO products (name, quantity, min_required) VALUES (?, ?, ?)";
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            stmt.setString(1, name);
//            stmt.setInt(2, qty);
//            stmt.setInt(3, minQty);
//            stmt.executeUpdate();
//            System.out.println("Product added.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void viewProducts() {
//        try (Connection conn = DBConnection.getConnection();
//             Statement stmt = conn.createStatement()) {
//            ResultSet rs = stmt.executeQuery("SELECT * FROM products");
//            while (rs.next()) {
//                System.out.printf("ID: %d | Name: %s | Qty: %d | Min: %d%n",
//                        rs.getInt("id"),
//                        rs.getString("name"),
//                        rs.getInt("quantity"),
//                        rs.getInt("min_required"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void deleteProduct(Scanner scanner) {
//        try (Connection conn = DBConnection.getConnection()) {
//            System.out.print("Enter product ID to delete: ");
//            int id = scanner.nextInt();
//            String sql = "DELETE FROM products WHERE id=?";
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            stmt.setInt(1, id);
//            stmt.executeUpdate();
//            System.out.println("Product deleted.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}





//package com.inventory;
//
//import java.sql.*;
//import java.util.InputMismatchException;
//import java.util.Scanner;
//
//public class AdminService {
//    public static void adminMenu(Scanner scanner) {
//        while (true) {
//            System.out.println("\nAdmin Menu");
//            System.out.println("1. Add Product");
//            System.out.println("2. View Products");
//            System.out.println("3. Delete Product");
//            System.out.println("4. Exit");
//            System.out.print("Choose an option: ");
//
//            // Handle integer input for choice safely
//            if (scanner.hasNextInt()) {
//                int choice = scanner.nextInt();
//                scanner.nextLine(); // Consume the newline left after reading integer
//                switch (choice) {
//                    case 1 -> addProduct(scanner);
//                    case 2 -> viewProducts();
//                    case 3 -> deleteProduct(scanner);
//                    case 4 -> { return; }
//                    default -> System.out.println("Invalid choice.");
//                }
//            } else {
//                System.out.println("Invalid input. Please enter a number.");
//                scanner.nextLine(); // Clear invalid input
//            }
//        }
//    }
//
//    private static void addProduct(Scanner scanner) {
//        // Use a temporary scanner to handle nested numeric input, or manage the main scanner carefully
//        // Since we pass the main scanner, we will rely on it.
//
//        try (Connection conn = DBConnection.getConnection()) {
//            System.out.print("Enter product name: ");
//            String name = scanner.nextLine();
//
//            System.out.print("Enter product price: "); // NEW: Prompt for price
//            double price = scanner.nextDouble();
//
//            System.out.print("Enter initial quantity: ");
//            int qty = scanner.nextInt();
//
//            System.out.print("Enter minimum required quantity (low stock threshold): ");
//            int minQty = scanner.nextInt();
//            scanner.nextLine(); // Consume newline after reading last integer
//
//            // MODIFIED SQL: Now inserts into (name, price, quantity, min_required)
//            String sql = "INSERT INTO products (name, price, quantity, min_required) VALUES (?, ?, ?, ?)";
//
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            stmt.setString(1, name);
//            stmt.setDouble(2, price); // SETTING PRICE
//            stmt.setInt(3, qty);
//            stmt.setInt(4, minQty);
//
//            stmt.executeUpdate();
//            System.out.println("✔ Product added successfully.");
//        } catch (InputMismatchException e) {
//            System.out.println("❌ Invalid input format. Please ensure quantity/price are numbers.");
//            // Clear the scanner buffer if a Mismatch occurred
//            if (scanner.hasNextLine()) {
//                scanner.nextLine();
//            }
//        } catch (SQLException e) {
//            System.out.println("❌ Database error adding product.");
//            e.printStackTrace();
//        } catch (Exception e) {
//            System.out.println("❌ An unexpected error occurred.");
//            e.printStackTrace();
//        }
//    }
//
//    private static void viewProducts() {
//        // Query now consistently uses the column name: min_required
//        String sql = "SELECT id, name, price, quantity, min_required, is_active FROM products";
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql);
//             ResultSet rs = stmt.executeQuery()) {
//
//            System.out.println("\n------------------------------------------------------");
//            System.out.println("                 Current Inventory");
//            System.out.println("------------------------------------------------------");
//            System.out.printf("%-4s | %-20s | %-6s | %-4s | %-3s | %-5s%n",
//                    "ID", "Name", "Price", "Qty", "Min", "Active");
//            System.out.println("------------------------------------------------------");
//
//            boolean found = false;
//            while (rs.next()) {
//                found = true;
//                System.out.printf("%-4d | %-20s | %-6.2f | %-4d | %-3d | %-5s%n",
//                        rs.getInt("id"),
//                        rs.getString("name"),
//                        rs.getDouble("price"),
//                        rs.getInt("quantity"),
//                        rs.getInt("min_required"),
//                        rs.getBoolean("is_active") ? "Yes" : "No");
//            }
//            if (!found) {
//                System.out.println("No products found in the inventory.");
//            }
//            System.out.println("------------------------------------------------------");
//
//        } catch (Exception e) {
//            System.out.println("❌ Database error during viewProducts.");
//            e.printStackTrace();
//        }
//    }
//
//    private static void deleteProduct(Scanner scanner) {
//        try (Connection conn = DBConnection.getConnection()) {
//            System.out.print("Enter product ID to delete: ");
//            // Handle integer input safely
//            if (!scanner.hasNextInt()) {
//                System.out.println("Invalid input. Please enter a valid ID number.");
//                scanner.nextLine();
//                return;
//            }
//            int id = scanner.nextInt();
//            scanner.nextLine(); // Consume newline
//
//            String sql = "DELETE FROM products WHERE id=?";
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            stmt.setInt(1, id);
//
//            int affectedRows = stmt.executeUpdate();
//
//            if (affectedRows > 0) {
//                System.out.println("✔ Product with ID " + id + " deleted successfully.");
//            } else {
//                System.out.println("❗ Product with ID " + id + " not found.");
//            }
//        } catch (Exception e) {
//            System.out.println("❌ Error deleting product.");
//            e.printStackTrace();
//        }
//    }
//}

//package com.inventory;
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//import org.springframework.stereotype.Service;
//
///**
// * Service layer responsible for business logic and database access for Admin tasks.
// * It is managed by Spring (@Service) and uses the ProductDto for data transfer.
// * All console-based methods (Scanner, System.out) have been removed.
// */
//@Service
//public class AdminService {
//
//    // --- Data Retrieval ---
//
//    /**
//     * Retrieves all products (both active and inactive) from the database.
//     * The controller can filter if necessary, but the service provides the raw data.
//     * @return A list of ProductDto objects.
//     */
//    public List<ProductDto> getAllProducts() {
//        List<ProductDto> products = new ArrayList<>();
//        // Fetch all fields, including is_active, for the Admin view
//        String sql = "SELECT id, name, price, quantity, min_required, is_active FROM products";
//
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql);
//             ResultSet rs = stmt.executeQuery()) {
//
//            while (rs.next()) {
//                ProductDto product = new ProductDto();
//                product.setId(rs.getInt("id"));
//                product.setName(rs.getString("name"));
//                product.setPrice(rs.getDouble("price"));
//                product.setQuantity(rs.getInt("quantity"));
//                product.setMin_required(rs.getInt("min_required"));
//                product.setIs_active(rs.getBoolean("is_active"));
//                products.add(product);
//            }
//        } catch (SQLException e) {
//            // Throw a custom runtime exception for the controller to handle gracefully
//            System.err.println("Database error during product retrieval: " + e.getMessage());
//            throw new RuntimeException("Error communicating with the inventory database.", e);
//        }
//        return products;
//    }
//
//    // --- Data Insertion ---
//
//    /**
//     * Inserts a new product into the database.
//     * @param productDto Data from the web request.
//     * @return The saved ProductDto, updated with the generated ID.
//     */
//    public ProductDto addProduct(ProductDto productDto) {
//        // We assume new products are active (TRUE) by default
//        String sql = "INSERT INTO products (name, price, quantity, min_required, is_active) VALUES (?, ?, ?, ?, TRUE)";
//
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//
//            // Set values from the DTO
//            stmt.setString(1, productDto.getName());
//            stmt.setDouble(2, productDto.getPrice());
//            stmt.setInt(3, productDto.getQuantity());
//            stmt.setInt(4, productDto.getMin_required());
//
//            int affectedRows = stmt.executeUpdate();
//
//            // Retrieve the auto-generated ID
//            if (affectedRows > 0) {
//                try (ResultSet keys = stmt.getGeneratedKeys()) {
//                    if (keys.next()) {
//                        productDto.setId(keys.getInt(1));
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Database error during product creation: " + e.getMessage());
//            throw new RuntimeException("Failed to add product due to a database issue.", e);
//        }
//        return productDto;
//    }
//
//    // --- Data Deletion (Soft Delete) ---
//
//    /**
//     * Deletes a product by setting its is_active flag to FALSE (Soft Delete).
//     * Hard deletes should generally be avoided in inventory systems.
//     * @param id The product ID to delete.
//     */
//    public void deleteProduct(int id) {
//        String sql = "UPDATE products SET is_active = FALSE WHERE id = ?";
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, id);
//            int updated = stmt.executeUpdate();
//
//            if (updated == 0) {
//                // Throwing an exception is good practice if a resource wasn't found
//                throw new RuntimeException("Product with ID " + id + " not found for deletion.");
//            }
//        } catch (SQLException e) {
//            System.err.println("Database error during soft deletion: " + e.getMessage());
//            throw new RuntimeException("Failed to delete product due to a database issue.", e);
//        }
//    }
//}





package com.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling all administrative product-related business logic.
 * This class now handles data using DTOs and throws exceptions instead of
 * interacting directly with the console.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    // Dependency injection is handled by Spring and @RequiredArgsConstructor
    // DBConnection must be configured as a Spring bean for this to work in a real app,
    // but we are using the static method for simplicity based on the existing structure.

    /**
     * Retrieves all active products from the database and maps them to DTOs.
     * @return A list of ProductDto objects.
     */
    public List<ProductDto> getAllProducts() {
        List<ProductDto> products = new ArrayList<>();
        // Note: is_active is included in the WHERE clause so the Admin can see inactive items.
        String sql = "SELECT id, name, price, quantity, min_required, is_active FROM products ORDER BY id ASC";

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
     * @param productDto The DTO containing the product details.
     * @return The DTO with the generated ID.
     */
    public ProductDto addProduct(ProductDto productDto) {
        // We assume new products are active (TRUE) by default
        String sql = "INSERT INTO products (name, price, quantity, min_required, is_active) VALUES (?, ?, ?, ?, TRUE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set values from the DTO
            stmt.setString(1, productDto.getName());
            stmt.setDouble(2, productDto.getPrice());
            stmt.setInt(3, productDto.getQuantity());
            // FIX: Corrected DTO getter from getMin_required() to getMinRequired()
            stmt.setInt(4, productDto.getMinRequired());

            int affectedRows = stmt.executeUpdate();

            // Retrieve the auto-generated ID
            if (affectedRows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        productDto.setId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during product creation: " + e.getMessage());
            throw new RuntimeException("Failed to add product due to a database issue.", e);
        }
        return productDto;
    }

    /**
     * Performs a soft delete (deactivates) a product.
     * @param id The ID of the product to deactivate.
     */
    public void deleteProduct(int id) {
        // Soft delete: Set is_active to FALSE instead of permanent deletion
        String sql = "UPDATE products SET is_active = FALSE WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                // If no rows were updated, the product ID didn't exist or was already inactive
                throw new RuntimeException("Product ID " + id + " not found or already inactive.");
            }
        } catch (SQLException e) {
            System.err.println("Database error during product deactivation: " + e.getMessage());
            throw new RuntimeException("Failed to deactivate product due to a database issue.", e);
        }
    }
}