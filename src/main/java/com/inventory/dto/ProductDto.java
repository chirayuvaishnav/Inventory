//package com.inventory;
//
//import lombok.Data; // Automatically generates getters, setters, constructors, etc.
//
///**
// * Data Transfer Object (DTO) for Product information.
// * Used to map JSON data from the frontend into Java objects, and vice versa.
// */
//@Data
//public class ProductDto {
//    private Integer id; // Nullable when creating a new product
//    private String name;
//    private Double price;
//    private Integer quantity;
//    private Integer min_required;
//    private Boolean is_active;
//}



package com.inventory.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for Product entity.
 * Used for transferring data between the web layer (JSON) and the service layer.
 * Lombok's @Data annotation generates getters, setters, equals, hashCode, and toString methods.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Integer id;
    private String name;
    private Double price;
    private Integer quantity;
    private Integer minRequired;
    private Boolean isActive;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getMinRequired() { return minRequired; }
    public void setMinRequired(Integer minRequired) { this.minRequired = minRequired; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}