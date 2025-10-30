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



package com.inventory;

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
    // FIXED: Standardized field name to CamelCase for correct Lombok setter/getter generation
    private Integer minRequired;
    private Boolean isActive;
}