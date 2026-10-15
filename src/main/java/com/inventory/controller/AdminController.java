package com.inventory.controller;

import com.inventory.dto.DemandAnalyticsDto;
import com.inventory.dto.ProductDto;
import com.inventory.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Maps to: GET /api/admin/products
     */
    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> products = adminService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Maps to: POST /api/admin/products
     */
    @PostMapping("/products")
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto) {
        ProductDto newProduct = adminService.addProduct(productDto);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    /**
     * Endpoint to restock a product.
     * Maps to: PUT /api/admin/products/restock/{productId}
     */
    @PutMapping("/products/restock/{productId}")
    public ResponseEntity<Void> restockProduct(@PathVariable int productId, @RequestBody Integer quantityToAdd) {
        if (quantityToAdd == null || quantityToAdd <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            adminService.updateQuantity(productId, quantityToAdd);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.err.println("Restock failed: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint to deactivate (soft delete) a product.
     * Maps to: DELETE /api/admin/products/{id}
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        try {
            adminService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.err.println("Deactivation failed: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint for historical sales & seasonal demand analytics.
     * Maps to: GET /api/admin/analytics/monthly?month=6&year=2024
     */
    @GetMapping("/analytics/monthly")
    public ResponseEntity<List<DemandAnalyticsDto>> getMonthlyDemandAnalytics(
            @RequestParam(required = false, defaultValue = "0") int month,
            @RequestParam(required = false, defaultValue = "0") int year) {
        if (month <= 0 || month > 12) {
            month = LocalDate.now().getMonthValue();
        }
        List<DemandAnalyticsDto> analytics = adminService.getMonthlyDemandAnalytics(month, year);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Endpoint for overall store KPI statistics, bestsellers, and monthly revenue trends.
     * Maps to: GET /api/admin/analytics/summary
     */
    @GetMapping("/analytics/summary")
    public ResponseEntity<Map<String, Object>> getOverallStoreStats() {
        Map<String, Object> stats = adminService.getOverallStoreStats();
        return ResponseEntity.ok(stats);
    }
}