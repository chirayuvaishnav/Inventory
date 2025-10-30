package com.inventory;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object for a single item within a Bill (Bill Item).
 * Used for receiving line item details from the frontend and recording them
 * in the bill_items table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillItemDto implements Serializable {
    private Integer productId;
    private Integer quantity;

    // The price and subtotal are typically calculated/validated on the backend
    // but the DTO holds the expected structure.
    private Double price;
    private Double subtotal;
}