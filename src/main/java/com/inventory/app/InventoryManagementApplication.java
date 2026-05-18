package com.inventory.app; // Ensure this package declaration is correct

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan; // <-- Import this!

@SpringBootApplication
@ComponentScan(basePackages = "com.inventory") // <-- ADD THIS LINE!
public class InventoryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryManagementApplication.class, args);
    }
}
