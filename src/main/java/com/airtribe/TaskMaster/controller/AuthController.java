package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.DTO.AuthRequest;
import com.airtribe.TaskMaster.dto.UserDTO;
import com.airtribe.TaskMaster.model.User;
import com.airtribe.TaskMaster.repository.TokenBlacklistRepository;
import com.airtribe.TaskMaster.service.JwtService;
import com.airtribe.TaskMaster.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for user authentication and management endpoints.
 */
@RestController
@RequestMapping("/api/*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    /**
     * Endpoint for user registration.
     * Maps to requirement 2: registration.
     */
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        try {
           // System.out.println("Registering user: " + userDTO.getUsername());
            User user = userService.registerUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "User registered successfully",
                    "username", user.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            // 1. Authenticate user credentials using the AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                // 2. If authenticated, generate JWT
                String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());
                return ResponseEntity.ok(Map.of("token", token));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials."));
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password."));
        }
    }

    /**
     * SECURED Endpoint for fetching the user's profile.
     * Maps to requirement 1: authorization check.
     * Maps to requirement 2: profile management (read).
     */
    @GetMapping("/user/profile")
    public ResponseEntity<?> getProfile() {
        // The user details are automatically available in the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated."));
        }

        // The principal is the User object (as it implements UserDetails)
        User user = (User) authentication.getPrincipal();

        // Return a map containing non-sensitive profile information
        return ResponseEntity.ok(Map.of(
                "id", user.getUserId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }

    /**
     * SECURED Endpoint for updating the user's profile.
     * Maps to requirement 2: profile management (update).
     */
    @PutMapping("/user/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserDTO updatedDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        try {
            User updatedUser = userService.updateProfile(user.getUsername(), updatedDetails);
            return ResponseEntity.ok(Map.of(
                    "message", "Profile updated successfully",
                    "username", updatedUser.getUsername()
            ));
        } catch (UsernameNotFoundException e) {
            // Should not happen if the user is authenticated, but good practice to handle
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> invalidateSession(@RequestHeader String authorization) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ){ //|| authentication.getPrincipal() instanceof String) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated."));
        }

        try {
            jwtService.clearSession(authorization);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "User Logged off."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }

    }
}