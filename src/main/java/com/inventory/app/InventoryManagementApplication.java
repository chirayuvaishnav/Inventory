package com.inventory.app; // Ensure this package declaration is correct

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.inventory")
@EnableScheduling // Enables Spring's scheduled task execution (e.g., automated stock alert jobs)
public class InventoryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryManagementApplication.class, args);
    }
}
