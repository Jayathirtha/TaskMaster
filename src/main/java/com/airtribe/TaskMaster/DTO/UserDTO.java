package com.airtribe.TaskMaster.dto;
import lombok.Data;

/**
 * DTO for user registration data.
 */
@Data
public class UserDTO {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
}