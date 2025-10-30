package com.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for the Cashier role.
 * Handles API requests related to sales, billing, and active inventory lookup.
 */
@RestController
@RequestMapping("/api/cashier")
@RequiredArgsConstructor
public class CashierController {

    private final CashierService cashierService;

    /**
     * Endpoint to get the list of active products available for sale.
     * Maps to: GET /api/cashier/products
     * @return List of ProductDto objects.
     */
    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getActiveProducts() {
        // Uses the service method we created to filter by is_active=TRUE and quantity > 0
        List<ProductDto> products = cashierService.getActiveProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Endpoint to process a new sale and create a bill.
     * Maps to: POST /api/cashier/bills
     * @param billDto The BillDto containing the list of items to be purchased.
     * @return The completed BillDto with the generated bill ID.
     */
    @PostMapping("/bills")
    public ResponseEntity<BillDto> createBill(@RequestBody BillDto billDto) {
        try {
            BillDto completedBill = cashierService.processSale(billDto);
            // Returns 201 Created status
            return new ResponseEntity<>(completedBill, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Catches validation errors (like insufficient stock) and returns 400 Bad Request
            System.err.println("Sale processing failed: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint to retrieve details of a specific bill.
     * Maps to: GET /api/cashier/bills/{billId}
     * @param billId The ID of the bill to retrieve.
     * @return The BillDto containing all details, or 404 if not found.
     */
    @GetMapping("/bills/{billId}")
    public ResponseEntity<BillDto> getBillDetails(@PathVariable int billId) {
        try {
            BillDto bill = cashierService.getBillById(billId);
            return ResponseEntity.ok(bill);
        } catch (RuntimeException e) {
            // Catches "Bill ID not found" and returns 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
}