package com.airtribe.TaskMaster.DTO;

import com.airtribe.TaskMaster.model.Task;

/** DTO for task response. */
public record TaskResponse(
        Long id,
        String title,
        String description,
        java.time.LocalDateTime dueDate,
        String status,
        java.time.LocalDateTime createdAt) {
    public TaskResponse(Task task) {
        this(task.getTaskId(), task.getTitle(), task.getDescription(), task.getDueDate(),
                task.getStatus().toString(), task.getCreatedAt());
    }
}