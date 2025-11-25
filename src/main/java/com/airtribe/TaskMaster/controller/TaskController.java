package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.DTO.TaskCreateRequest;
import com.airtribe.TaskMaster.DTO.TaskResponse;
import com.airtribe.TaskMaster.DTO.TaskUpdateRequest;
import com.airtribe.TaskMaster.model.Task;
import com.airtribe.TaskMaster.model.User;
import com.airtribe.TaskMaster.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST Controller for Task Management operations.
 * All endpoints require authentication (handled by SecurityConfig).
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** Helper method to get the ID of the currently authenticated user. */
    private Long getCurrentUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskCreateRequest request) {
        Long userId = getCurrentUserId();
        Task task = taskService.createTask(userId, request);
        return new ResponseEntity<>(new TaskResponse(task), HttpStatus.CREATED);
    }

    /**
     * 2. READ Task (Get Single)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        try {
            Task task = taskService.getTaskById(id, getCurrentUserId());
            return ResponseEntity.ok(new TaskResponse(task));
        } catch (NoSuchElementException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 3. UPDATE Task
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @RequestBody TaskUpdateRequest request) {
        try {
            Task updatedTask = taskService.updateTask(id, getCurrentUserId(), request);
            return ResponseEntity.ok(new TaskResponse(updatedTask));
        } catch (NoSuchElementException e) {
            return new ResponseEntity("Task not found or unauthorized access.", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 4. DELETE Task
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<TaskResponse> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id, getCurrentUserId());
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            // Return 204 No Content even if not found for idempotency, or 404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 5. LIST/FILTER/SORT/SEARCH Tasks
     * Parameters are optional for comprehensive task management.
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "createdAtDesc") String sortBy) {

        List<Task> tasks = taskService.findTasksByCriteria(getCurrentUserId(), status, search, sortBy);

        List<TaskResponse> responseList = tasks.stream()
                .map(TaskResponse::new)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/list")
    public ResponseEntity<List<TaskResponse>> getAllTasksByUser() {
        try {
            List<Task> tasks = taskService.getTaskByUserId(getCurrentUserId());
            List<TaskResponse> responseList = tasks.stream()
                    .map(TaskResponse::new)
                    .toList();

            return ResponseEntity.ok(responseList);
        } catch (NoSuchElementException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}