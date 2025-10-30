package com.inventory;

import lombok.RequiredArgsConstructor;
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
 */
@Service
@RequiredArgsConstructor
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

    /**
     * Performs a SOFT DELETE (deactivates) a product by setting is_active = FALSE.
     */
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

    /**
     * Updates the quantity of a product by adding the specified restock amount.
     */
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
        } catch (SQLException e) {
            System.err.println("Database error during restock: " + e.getMessage());
            throw new RuntimeException("Failed to restock product.", e);
        }
    }
}