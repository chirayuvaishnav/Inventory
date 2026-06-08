//package com.inventory.model;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class DBConnection {
//    private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
//    private static final String USER = "root";
//    private static final String PASSWORD = "Chirayu@123";
//
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(URL, USER, PASSWORD);
//    }
//}

package com.inventory.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Dynamically fetch environment variables from Render, or use localhost as a fallback for local testing
    private static final String HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String PORT = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
    private static final String DB_NAME = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "inventory_db";
    private static final String USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "Chirayu@123";

    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load the MySQL Driver class to ensure compatibility inside Docker containers
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found instance context", e);
        }

        // Construct the URL dynamically. If it's connecting to Aiven, append the mandatory SSL parameters.
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME;
        if (!HOST.equals("localhost")) {
            url += "?sslMode=PREFERRED&allowPublicKeyRetrieval=true";
        }

        return DriverManager.getConnection(url, USER, PASSWORD);
    }
}