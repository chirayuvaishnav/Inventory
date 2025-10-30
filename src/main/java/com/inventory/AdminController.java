package com.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing product inventory (Admin tasks).
 * Handles HTTP requests from the frontend and delegates business logic to AdminService.
 */
@RestController // Marks this class as a Spring component that handles REST requests
@RequestMapping("/api/admin/products") // Base URL for all endpoints in this controller
@CrossOrigin(origins = "http://localhost:8080") // Allows the frontend (running locally on port 8080) to access this API
public class AdminController {

    private final AdminService adminService;

    // Dependency Injection: Spring automatically provides the AdminService instance
    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Endpoint to retrieve all products.
     * Mapped to: GET /api/admin/products
     * @return A list of products (sent as JSON to the frontend).
     */
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        try {
            List<ProductDto> products = adminService.getAllProducts();
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Catches exceptions thrown by the AdminService (e.g., database connection issues)
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint to add a new product.
     * Mapped to: POST /api/admin/products
     * Request body (JSON) is automatically converted to ProductDto by Spring.
     * @param productDto The product data received from the request body.
     * @return The created ProductDto with the new ID.
     */
    @PostMapping
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto) {
        try {
            ProductDto createdProduct = adminService.addProduct(productDto);
            // Returns 201 Created status code
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint to delete (soft-delete) a product by ID.
     * Mapped to: DELETE /api/admin/products/{id}
     * @param id The ID of the product to delete, extracted from the URL path.
     * @return A 204 No Content status on success, or 404/500 on failure.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        try {
            adminService.deleteProduct(id);
            // Returns 204 No Content, meaning the action was successful and there's no body to return.
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            // If deleteProduct throws an exception (e.g., product not found or DB error)
            // We can check the message, but for simplicity, we return a general error.
            if (e.getMessage().contains("not found")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
