package com.inventory;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for User Authentication.
 * This class is used for two purposes:
 * 1. Receiving username and password from the frontend (for login validation).
 * 2. Returning the validated user's username and assigned role to the frontend (for redirection).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;

    // Used when receiving data from the frontend login form
    private String password;

    // Used when returning data (role) to the frontend after successful validation
    private String role;
}
