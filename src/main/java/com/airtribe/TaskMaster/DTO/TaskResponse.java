package com.airtribe.TaskMaster.DTO;

import com.airtribe.TaskMaster.model.Task;

import java.time.LocalDate;

/** DTO for task response. */
public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        String status,
        LocalDate createdAt) {
    public TaskResponse(Task task) {
        this(task.getTaskId(), task.getTitle(), task.getDescription(), task.getDueDate(),
                task.getStatus().toString(), task.getCreatedAt());
    }
}