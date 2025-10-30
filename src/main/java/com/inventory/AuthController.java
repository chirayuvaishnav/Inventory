package com.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for user authentication.
 * Exposes the /api/auth/login endpoint for the frontend.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Handles the login request from the frontend.
     * Maps to: POST /api/auth/login
     * @param userDto DTO containing the username and password.
     * @return UserDto with the assigned role upon success, or 401 Unauthorized.
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody UserDto userDto) {
        try {
            // Call the service to validate credentials
            UserDto authenticatedUser = authService.authenticate(userDto.getUsername(), userDto.getPassword());

            // If successful, return the user's role and username (password is already null in the DTO)
            return ResponseEntity.ok(authenticatedUser);

        } catch (RuntimeException e) {
            // Catches "Invalid username or password." error from AuthService
            System.err.println("Login attempt failed: " + e.getMessage());
            // Return 401 Unauthorized status
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}