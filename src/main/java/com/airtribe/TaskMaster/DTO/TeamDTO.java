package com.airtribe.TaskMaster.DTO;

import lombok.Data;
import java.util.List;

/**
 * DTO for Team creation and update.
 */
@Data
public class TeamDTO {
    private String name;
    private String description;
    private List<String> memberUsernames; // Usernames to add/invite
}