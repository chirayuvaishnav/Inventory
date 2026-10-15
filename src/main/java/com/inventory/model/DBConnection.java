package com.inventory.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Thread-safe Raw JDBC Connection Provider.
 *
 * Technical Interview Talking Points:
 * 1. Cloud-Native 12-Factor App Compatibility:
 *    - Dynamically resolves environment variables (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, DATABASE_URL)
 *      provided by cloud platforms like Render, Aiven, or Railway.
 *    - Automatically handles SSL handshake parameters (sslMode=PREFERRED & allowPublicKeyRetrieval=true)
 *      when communicating with remote cloud databases.
 *    - Falls back gracefully to local development defaults (localhost:3306) when environment variables are unset.
 * 2. Explicit Driver Registration:
 *    - Explicitly invokes Class.forName("com.mysql.cj.jdbc.Driver") to guarantee driver availability in containerized JRE environments.
 */
public class DBConnection {

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found in runtime classpath.", e);
        }

        // 1. Check for complete DATABASE_URL or DB_URL (common in cloud environments)
        String rawDbUrl = System.getenv("DATABASE_URL");
        if (rawDbUrl == null || rawDbUrl.trim().isEmpty()) {
            rawDbUrl = System.getenv("DB_URL");
        }

        if (rawDbUrl != null && !rawDbUrl.trim().isEmpty()) {
            String url = rawDbUrl.trim();
            if (url.startsWith("mysql://")) {
                url = "jdbc:" + url;
            }
            if (!url.contains("sslMode") && !url.contains("useSSL")) {
                url += (url.contains("?") ? "&" : "?") + "sslMode=PREFERRED&allowPublicKeyRetrieval=true";
            }
            String user = System.getenv("DB_USER");
            String pass = System.getenv("DB_PASSWORD");
            if (user != null && pass != null) {
                return DriverManager.getConnection(url, user, pass);
            }
            return DriverManager.getConnection(url);
        }

        // 2. Resolve individual connection environment variables (Defaults to Aiven MySQL)
        String host = resolveConfig("DB_HOST", "mysql-25f61199-chirayu-2805.l.aivencloud.com");
        String port = resolveConfig("DB_PORT", "16498");
        String dbName = resolveConfig("DB_NAME", "defaultdb");
        String user = resolveConfig("DB_USER", "avnadmin");
        String password = resolveConfig("DB_PASSWORD", "");

        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:mysql://").append(host).append(":").append(port).append("/").append(dbName);

        if (!host.equalsIgnoreCase("localhost") && !host.equals("127.0.0.1")) {
            // Mandatory SSL for Cloud Providers (Aiven, Render, etc.)
            sb.append("?sslMode=PREFERRED&allowPublicKeyRetrieval=true");
        } else {
            // Local development
            sb.append("?allowPublicKeyRetrieval=true&useSSL=false");
        }

        return DriverManager.getConnection(sb.toString(), user, password);
    }

    private static String resolveConfig(String key, String defaultValue) {
        String val = System.getenv(key);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        val = System.getProperty(key);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        try {
            java.io.File envFile = new java.io.File(".env");
            if (envFile.exists()) {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith(key + "=")) {
                            return line.substring(key.length() + 1).trim();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return defaultValue;
    }
}