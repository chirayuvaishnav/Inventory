package com.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Maps to: GET /api/admin/products
     */
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        // Calls the service method that filters for active products
        List<ProductDto> products = adminService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Maps to: POST /api/admin/products
     */
    @PostMapping
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto) {
        ProductDto newProduct = adminService.addProduct(productDto);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    /**
     * Endpoint to restock a product.
     * Maps to: PUT /api/admin/products/restock/{productId}
     * * NOTE: We rely on the frontend to pass an Integer representing the restock quantity.
     */
    @PutMapping("/restock/{productId}")
    public ResponseEntity<Void> restockProduct(@PathVariable int productId, @RequestBody Integer quantityToAdd) {
        if (quantityToAdd == null || quantityToAdd <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            adminService.updateQuantity(productId, quantityToAdd);
            return ResponseEntity.ok().build(); // 200 OK
        } catch (RuntimeException e) {
            // Catches "Product ID not found" from service
            System.err.println("Restock failed: " + e.getMessage());
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    /**
     * Endpoint to deactivate (soft delete) a product.
     * Maps to: DELETE /api/admin/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        try {
            adminService.deleteProduct(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (RuntimeException e) {
            // Catches "Product ID not found" from service
            System.err.println("Deactivation failed: " + e.getMessage());
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}