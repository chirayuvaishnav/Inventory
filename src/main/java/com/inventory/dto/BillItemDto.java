package com.inventory.dto;

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

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}