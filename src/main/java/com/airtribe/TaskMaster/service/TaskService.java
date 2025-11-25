package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.DTO.TaskCreateRequest;
import com.airtribe.TaskMaster.DTO.TaskUpdateRequest;
import com.airtribe.TaskMaster.model.Task;
import com.airtribe.TaskMaster.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service layer for Task CRUD and management operations.
 * Enforces task ownership by always checking against the provided userId.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // --- CRUD Operations ---

    @Transactional
    public Task createTask(Long userId, TaskCreateRequest request) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        // Status defaults to PENDING in the entity model
        return taskRepository.save(task);
    }

    public Task getTaskById(Long taskId, Long userId) {
        // Ensure the task exists AND belongs to the user
        return taskRepository.findByTaskIdAndUserId(taskId, userId)
                .orElseThrow(() -> new NoSuchElementException("Task not found or unauthorized access."));
    }

    public List<Task> getTaskByUserId(Long userId) {
        // Ensure the task exists AND belongs to the user
        return taskRepository.findAllByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Task not found or unauthorized access."));
    }


    @Transactional
    public Task updateTask(Long taskId, Long userId, TaskUpdateRequest request) {
        Task existingTask = getTaskById(taskId, userId); // Uses the security-checked getter

        existingTask.setTitle(request.title());
        existingTask.setDescription(request.description());
        existingTask.setDueDate(request.dueDate());

        // Update status if provided
        if (request.status() != null) {
            existingTask.setStatus(Task.Status.valueOf(request.status()));
        }

        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long taskId, Long userId) {
        Task existingTask = getTaskById(taskId, userId);

        try {
             taskRepository.deleteTaskByTaskIdAndUserId(taskId, userId);
       } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Task> findTasksByCriteria(Long currentUserId, String status, String search, String sortBy) {

        try {
           return  taskRepository.findByCriteria(currentUserId, Task.Status.valueOf(status), search, sortBy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
