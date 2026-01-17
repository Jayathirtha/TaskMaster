package com.airtribe.TaskMaster.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.airtribe.TaskMaster.DTO.CommentDTO;
import com.airtribe.TaskMaster.DTO.TaskDTO;
import com.airtribe.TaskMaster.DTO.TaskFilterDTO;
import com.airtribe.TaskMaster.model.Comment;
import com.airtribe.TaskMaster.model.Task;
import com.airtribe.TaskMaster.model.User;
import com.airtribe.TaskMaster.service.TaskService;

/**
 * REST Controller for Task and Comment Management.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;


    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ) { // || !(authentication.getPrincipal() instanceof UserPrincipal)
            throw new SecurityException("User not authenticated.");
        }
        return  ((User) Objects.requireNonNull(authentication.getPrincipal())).getUsername();
    }

    // --- Task Endpoints ---

    /**
     * Creates a new task within a project.
     * POST /api/tasks
     */
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO) {
        try {
            Task task = taskService.createTask(taskDTO, getCurrentUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Task created successfully",
                    "taskId", task.getTaskId(),
                    "title", task.getTitle()
            ));
        } catch (SecurityException | IllegalArgumentException | UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Assigns a task to a user.
     * PATCH /api/tasks/{taskId}/assign?username=john.doe
     */
    @PatchMapping("/{taskId}/assign")
    public ResponseEntity<?> assignTask(@PathVariable Long taskId, @RequestParam String username) {
        try {
            Task task = taskService.assignTask(taskId, username, getCurrentUsername());
            return ResponseEntity.ok(Map.of(
                    "message", "Task " + taskId + " assigned to " + username,
                    "assignee", task.getAssignee().getUsername()
            ));
        } catch (SecurityException | IllegalArgumentException | UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Updates the status of a task.
     * PATCH /api/tasks/{taskId}/status?status=COMPLETE
     */
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long taskId, @RequestParam String status) {
        try {
            Task.Status newStatus = Task.Status.valueOf(status.toUpperCase());
            Task task = taskService.updateTaskStatus(taskId, newStatus, getCurrentUsername());
            return ResponseEntity.ok(Map.of(
                    "message", "Task " + taskId + " status updated to " + newStatus.name(),
                    "status", task.getStatus().name()
            ));
        } catch (SecurityException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Retrieves all tasks assigned to the current user.
     * GET /api/tasks/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<Task>> getMyAssignedTasks() {
        try {
            List<Task> tasks = taskService.getTasksAssignedToUser(getCurrentUsername());
            return ResponseEntity.ok(tasks);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Retrieves all tasks for a specific project.
     * GET /api/tasks/project/{projectId}
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTasksByProject(@PathVariable Long projectId) {
        try {
            List<Task> tasks = taskService.getTasksByProject(projectId, getCurrentUsername());
            return ResponseEntity.ok(tasks);
        } catch (SecurityException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/tasks/filter
     * 
     * Query Parameters (all optional):
     * Examples:
     * - /api/tasks/filter?status=OPEN
     * - /api/tasks/filter?searchKeyword=bug&status=OPEN
     * - /api/tasks/filter?overdue=true
     * - /api/tasks/filter?assigneeUsername=john&sortBy=dueDate&sortDirection=ASC
     */
    @GetMapping("/filter")
    public ResponseEntity<?> filterTasks(
            @RequestParam(required = false) Task.Status status,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String assigneeUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false) Boolean unassigned,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        
        try {
            TaskFilterDTO filterDTO = new TaskFilterDTO();
            filterDTO.setStatus(status);
            filterDTO.setSearchKeyword(searchKeyword);
            filterDTO.setProjectId(projectId);
            filterDTO.setAssigneeId(assigneeId);
            filterDTO.setAssigneeUsername(assigneeUsername);
            filterDTO.setDueDateFrom(dueDateFrom);
            filterDTO.setDueDateTo(dueDateTo);
            filterDTO.setCreatedFrom(createdFrom);
            filterDTO.setCreatedTo(createdTo);
            filterDTO.setOverdue(overdue);
            filterDTO.setUnassigned(unassigned);
            filterDTO.setSortBy(sortBy);
            filterDTO.setSortDirection(sortDirection);

            List<Task> tasks = taskService.filterTasks(filterDTO);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Quick search tasks by keyword.
     * GET /api/tasks/quick-search?keyword=bug
     */
    @GetMapping("/quick-search")
    public ResponseEntity<?> quickSearch(@RequestParam String keyword) {
        try {
            List<Task> tasks = taskService.searchTasks(keyword);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all overdue tasks.
     * GET /api/tasks/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        List<Task> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get all unassigned tasks.
     * GET /api/tasks/unassigned
     */
    @GetMapping("/unassigned")
    public ResponseEntity<List<Task>> getUnassignedTasks() {
        List<Task> tasks = taskService.getUnassignedTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Legacy search endpoint (deprecated - use /filter instead).
     * GET /api/tasks/search
     */
    @Deprecated
    @GetMapping("/search")
    public ResponseEntity<?> getTasksByCriteria(@RequestParam String status,@RequestParam String searchItem) {
        if((status != null && !status.isEmpty())) {
            try {
                List<Task> tasks = taskService.getTasksByStatus(Task.Status.valueOf(status));
                return ResponseEntity.ok(tasks);
            } catch (SecurityException | IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
        }else {
            try {
                List<Task> tasks = taskService.getTasksBySearchItem(searchItem);
                return ResponseEntity.ok(tasks);
            } catch (SecurityException | IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
        }
    }

    // --- Comment Endpoints ---

    /**
     * Adds a comment (with optional attachment metadata) to a task.
     * POST /api/tasks/{taskId}/comments
     */
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long taskId, @RequestBody CommentDTO commentDTO) {
        try {
            // Ensure commentDTO knows which task it belongs to
            commentDTO.setTaskId(taskId);

            Comment comment = taskService.addComment(commentDTO, getCurrentUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Comment added successfully",
                    "commentId", comment.getCommentId(),
                    "taskId", taskId
            ));
        } catch (SecurityException | IllegalArgumentException | UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Retrieves all comments for a specific task.
     * GET /api/tasks/{taskId}/comments
     */
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long taskId) {
        try {
            List<Comment> comments = taskService.getCommentsByTask(taskId);
            return ResponseEntity.ok(comments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

}