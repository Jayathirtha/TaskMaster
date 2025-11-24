package com.airtribe.TaskMaster.service;
import com.airtribe.TaskMaster.dto.UserDTO;
import com.airtribe.TaskMaster.model.User;
import com.airtribe.TaskMaster.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for user management and implements UserDetailsService for Spring Security.
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * Implementation of UserDetailsService method to load user by username (email) for security.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userDetail = repository.findByUsername(username);
        return userDetail.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Handles user registration, including secure password hashing.
     */
    public User registerUser(UserDTO userDTO) {
        if (repository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        User newUser = User.builder()
                .username(userDTO.getUsername())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                // Secure Password Hashing
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role("USER")
                .build();

        return repository.save(newUser);
    }

    /**
     * Updates an existing user's profile information.
     */
    public User updateProfile(String username, UserDTO updatedDetails) {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Update fields
        if (updatedDetails.getFirstName() != null) {
            user.setFirstName(updatedDetails.getFirstName());
        }
        if (updatedDetails.getLastName() != null) {
            user.setLastName(updatedDetails.getLastName());
        }
        // Note: Password change requires a separate, secure process (not included here for brevity)

        return repository.save(user);
    }
}