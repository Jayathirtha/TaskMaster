package com.airtribe.TaskMaster.DTO;

import lombok.Data;

/**
 * DTO for receiving login credentials (username and raw password).
 */
@Data
public class AuthRequest {
    private String username;
    private String password;
}