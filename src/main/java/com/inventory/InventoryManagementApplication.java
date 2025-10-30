package com.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Spring Boot application.
 * - @SpringBootApplication enables autoconfiguration, component scanning, and configuration setup.
 * - It will automatically scan components (like controllers and services) in the 'com.inventory' package
 * and its subpackages.
 */
@SpringBootApplication
public class InventoryManagementApplication {

    public static void main(String[] args) {
        // This line starts the embedded web server (Tomcat) and initializes the Spring context.
        SpringApplication.run(InventoryManagementApplication.class, args);
    }

}
