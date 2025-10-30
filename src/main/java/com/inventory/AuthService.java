//package com.inventory;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//
//public class AuthService {
//    public static User login(String username, String password) {
//        try (Connection conn = DBConnection.getConnection()) {
//            String sql = "SELECT * FROM users WHERE username=? AND password=?";
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            stmt.setString(1, username);
//            stmt.setString(2, password);
//            ResultSet rs = stmt.executeQuery();
//            if (rs.next()) {
//                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}




package com.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service class dedicated to user authentication and role verification.
 * It queries the 'users' table directly.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    // Note: In a real-world application, passwords would be hashed (e.g., using BCrypt),
    // and DBConnection would be injected, but for simplicity, we use the static check here.

    /**
     * Validates user credentials against the database.
     * @param username The username provided by the user.
     * @param password The password provided by the user.
     * @return A UserDto containing the username and assigned role upon successful login.
     * @throws RuntimeException if the login fails due to invalid credentials or database error.
     */
    public UserDto authenticate(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");

                    // Successful login: return the role and username
                    UserDto user = new UserDto();
                    user.setUsername(username);
                    user.setRole(role);
                    // Do NOT return the password
                    user.setPassword(null);

                    return user;
                } else {
                    // Login failed (username/password mismatch)
                    throw new RuntimeException("Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            throw new RuntimeException("Authentication failed due to a database issue.", e);
        }
    }
}