package com.airtribe.TaskMaster.service.impl;

import com.airtribe.TaskMaster.DTO.CommentDTO;
import com.airtribe.TaskMaster.DTO.TaskDTO;
import com.airtribe.TaskMaster.DTO.TaskFilterDTO;
import com.airtribe.TaskMaster.model.*;
import com.airtribe.TaskMaster.repository.CommentRepository;
import com.airtribe.TaskMaster.repository.ProjectRepository;
import com.airtribe.TaskMaster.repository.TaskRepository;
import com.airtribe.TaskMaster.repository.UserRepository;
import com.airtribe.TaskMaster.service.TaskService;
import com.airtribe.TaskMaster.service.TeamService;
import com.airtribe.TaskMaster.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Task and Comment Management. (SRP: Task Workflow)
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CommentRepository commentRepository;

    // --- Task Management ---

    private void checkProjectAccess(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        // Re-use TeamService to check if user belongs to the project's team
        Team team = project.getTeam();
        boolean isMember = team.getMembers().stream()
                .anyMatch(u -> u.getUsername().equals(username));

        if (!isMember) {
            throw new SecurityException("User " + username + " does not have access to project " + projectId);
        }
    }


    @Override
    @Transactional
    public Task createTask(TaskDTO taskDTO, String creatorUsername) {
        checkProjectAccess(taskDTO.getProjectId(), creatorUsername);

        Project project = projectRepository.findById(taskDTO.getProjectId()).get();

        User assignee = null;
        if (taskDTO.getAssigneeUsername() != null) {
            assignee = userRepository.findByUsername(taskDTO.getAssigneeUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Assignee user not found: " + taskDTO.getAssigneeUsername()));
        }

        Task newTask = Task.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .dueDate(taskDTO.getDueDate())
                .project(project)
                .assignee(assignee)
                .createdAt(LocalDateTime.now())
                .status(Task.Status.OPEN)
                .build();

        return taskRepository.save(newTask);
    }


    @Override
    @Transactional
    public Task assignTask(Long taskId, String assigneeUsername, String principalUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        checkProjectAccess(task.getProject().getId(), principalUsername);

        User newAssignee = userRepository.findByUsername(assigneeUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Assignee user not found: " + assigneeUsername));
        task.setAssignee(newAssignee);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task updateTaskStatus(Long taskId, Task.Status newStatus, String principalUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        checkProjectAccess(task.getProject().getId(), principalUsername); // Ensure principal has access

        task.setStatus(newStatus);
        return taskRepository.save(task);
    }

    @Override
    public Optional<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }

    @Override
    public List<Task> getTasksByProject(Long projectId, String principalUsername) {
        checkProjectAccess(projectId, principalUsername);
        return taskRepository.findByProjectId(projectId);
    }

    @Override
    public List<Task> getTasksAssignedToUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return taskRepository.findByAssignee(user);
    }

    // --- Comment Management ---

    @Override
    @Transactional
    public Comment addComment(CommentDTO commentDTO, String authorUsername) {
        Task task = taskRepository.findById(commentDTO.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + commentDTO.getTaskId()));

        checkProjectAccess(task.getProject().getId(), authorUsername); // Ensure author has access

        User author = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Author not found: " + authorUsername));

        Comment newComment = Comment.builder()
                .content(commentDTO.getContent())
                .attachmentFileName(commentDTO.getAttachmentFileName()) // Attachment
                .task(task)
                .author(author)
                .build();

        Comment savedComment = commentRepository.save(newComment);
        task.getComments().add(savedComment);

        return savedComment;
    }

    @Override
    public List<Comment> getCommentsByTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        return task.getComments().stream().collect(Collectors.toList());
    }

    @Override
    public List<Task> getTasksByStatus(Task.Status taskStatus) {
        if(taskStatus != null) {
            try {
                return taskRepository.findTaskByStatus(taskStatus);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<Task> getTasksBySearchItem(String searchItem) {
        if(searchItem != null) {
            try {
                return taskRepository.findTaskByTitleOrDescription(searchItem);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return Collections.emptyList();
    }

    // --- New Dynamic Filtering and Searching ---

    @Override
    public List<Task> filterTasks(TaskFilterDTO filterDTO) {
        // Build specification from filter criteria
        Specification<Task> spec = TaskSpecification.filterTasks(
                filterDTO.getStatus(),
                filterDTO.getSearchKeyword(),
                filterDTO.getProjectId(),
                filterDTO.getAssigneeId(),
                filterDTO.getDueDateFrom(),
                filterDTO.getDueDateTo(),
                filterDTO.getCreatedFrom(),
                filterDTO.getCreatedTo()
        );

        // Add special filters
        if (Boolean.TRUE.equals(filterDTO.getOverdue())) {
            spec = spec.and(TaskSpecification.isOverdue());
        }
        if (Boolean.TRUE.equals(filterDTO.getUnassigned())) {
            spec = spec.and(TaskSpecification.isUnassigned());
        }
        if (filterDTO.getAssigneeUsername() != null && !filterDTO.getAssigneeUsername().trim().isEmpty()) {
            spec = spec.and(TaskSpecification.assignedToUser(filterDTO.getAssigneeUsername()));
        }

        // Apply sorting
        Sort sort = buildSort(filterDTO.getSortBy(), filterDTO.getSortDirection());
        
        return taskRepository.findAll(spec, sort);
    }

    @Override
    public List<Task> searchTasks(String keyword) {
        Specification<Task> spec = TaskSpecification.searchByKeyword(keyword);
        return taskRepository.findAll(spec);
    }

    @Override
    public List<Task> getOverdueTasks() {
        Specification<Task> spec = TaskSpecification.isOverdue();
        return taskRepository.findAll(spec);
    }

    @Override
    public List<Task> getUnassignedTasks() {
        Specification<Task> spec = TaskSpecification.isUnassigned();
        return taskRepository.findAll(spec);
    }

    /**
     * Helper method to build sorting criteria.
     */
    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt"); // Default sort
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }

}
