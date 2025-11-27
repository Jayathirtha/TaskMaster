package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.DTO.CommentDTO;
import com.airtribe.TaskMaster.DTO.TaskDTO;
import com.airtribe.TaskMaster.model.Comment;
import com.airtribe.TaskMaster.model.Task;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Interface for Task and Comment Management.
 */
public interface TaskService {
    // Task Management
    Task createTask(TaskDTO taskDTO, String creatorUsername);
    Task assignTask(Long taskId, String assigneeUsername, String principalUsername);
    Task updateTaskStatus(Long taskId, Task.Status newStatus, String principalUsername);
    Optional<Task> getTaskById(Long taskId);
    List<Task> getTasksByProject(Long projectId, String principalUsername);
    List<Task> getTasksAssignedToUser(String username);

    // Comment Management
    Comment addComment(CommentDTO commentDTO, String authorUsername);
    List<Comment> getCommentsByTask(Long taskId);
}