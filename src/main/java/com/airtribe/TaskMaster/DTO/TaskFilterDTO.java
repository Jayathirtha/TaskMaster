package com.airtribe.TaskMaster.DTO;

import com.airtribe.TaskMaster.model.Task;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for filtering and searching tasks.
 * All fields are optional - only provided fields will be used as filters.
 */
@Data
public class TaskFilterDTO {
    
    // Filter by task status
    private Task.Status status;
    
    // Search keyword (searches in title and description)
    private String searchKeyword;
    
    // Filter by project ID
    private Long projectId;
    
    // Filter by assignee user ID
    private Long assigneeId;
    
    // Filter by assignee username
    private String assigneeUsername;
    
    // Due date range filters
    private LocalDateTime dueDateFrom;
    private LocalDateTime dueDateTo;
    
    // Creation date range filters
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    
    // Special filters
    private Boolean overdue;      // Find overdue tasks
    private Boolean unassigned;   // Find unassigned tasks
    
    // Sorting
    private String sortBy;        // Field to sort by (e.g., "dueDate", "createdAt", "title")
    private String sortDirection; // "ASC" or "DESC"
}
