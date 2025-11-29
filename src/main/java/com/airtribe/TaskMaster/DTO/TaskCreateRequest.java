package com.airtribe.TaskMaster.DTO;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** DTO for creating a new task. */
public record TaskCreateRequest(
        String title,
        String description,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {}
