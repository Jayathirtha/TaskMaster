package com.airtribe.TaskMaster.DTO;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** DTO for updating an existing task. */
public record TaskUpdateRequest(
        String title,
        String description,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
        String status) {}