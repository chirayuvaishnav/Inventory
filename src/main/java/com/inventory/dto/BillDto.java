package com.inventory.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object for the entire Bill (Sale Transaction Header).
 * This DTO is used to receive the list of items purchased from the frontend
 * and to return the final details of the recorded bill.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDto implements Serializable {
    private Integer billId;
    private Double totalAmount;
    private String billDate; // To hold the timestamp/date for viewing

    /**
     * The core of the Bill: A list of all individual products and quantities sold.
     * This is crucial for receiving data from the frontend.
     */
    private List<BillItemDto> items;

    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getBillDate() { return billDate; }
    public void setBillDate(String billDate) { this.billDate = billDate; }

    public List<BillItemDto> getItems() { return items; }
    public void setItems(List<BillItemDto> items) { this.items = items; }
}