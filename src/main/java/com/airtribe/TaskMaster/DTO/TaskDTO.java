package com.airtribe.TaskMaster.DTO;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for Task creation and update.
 */
@Data
public class TaskDTO {
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private String status; // For updates
    private String assigneeUsername; // For task assignment
    private Long projectId;
}