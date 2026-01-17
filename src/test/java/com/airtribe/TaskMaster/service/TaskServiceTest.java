package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.DTO.TaskDTO;
import com.airtribe.TaskMaster.DTO.TaskFilterDTO;
import com.airtribe.TaskMaster.model.*;
import com.airtribe.TaskMaster.repository.ProjectRepository;
import com.airtribe.TaskMaster.repository.TaskRepository;
import com.airtribe.TaskMaster.repository.UserRepository;
import com.airtribe.TaskMaster.service.impl.TaskServiceImpl;
import com.airtribe.TaskMaster.specification.TaskSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskServiceImpl.
 * Tests task creation, assignment, filtering, and search functionality.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User testUser;
    private User assigneeUser;
    private Project testProject;
    private Task testTask;
    private Team testTeam;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .userId(1L)
                .username("testuser@example.com")
                .firstName("Test")
                .lastName("User")
                .teams(new HashSet<>())
                .build();

        // Setup assignee user
        assigneeUser = User.builder()
                .userId(2L)
                .username("assignee@example.com")
                .firstName("Assignee")
                .lastName("User")
                .teams(new HashSet<>())
                .build();

        // Setup test team
        testTeam = Team.builder()
                .teamId(1L)
                .name("Test Team")
                .members(new HashSet<>(Arrays.asList(testUser, assigneeUser)))
                .build();

        testUser.getTeams().add(testTeam);
        assigneeUser.getTeams().add(testTeam);

        // Setup test project
        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .team(testTeam)
                .build();

        // Setup test task
        testTask = Task.builder()
                .taskId(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.Status.OPEN)
                .project(testProject)
                .assignee(assigneeUser)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();
    }







    @Test
    void testFilterTasks_ByStatus() {
        // Arrange
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setStatus(Task.Status.OPEN);

        Task openTask = Task.builder()
                .taskId(2L)
                .title("Open Task")
                .status(Task.Status.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(openTask));

        // Act
        List<Task> result = taskService.filterTasks(filterDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Task.Status.OPEN, result.get(0).getStatus());
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void testFilterTasks_BySearchKeyword() {
        // Arrange
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setSearchKeyword("bug");

        Task bugTask = Task.builder()
                .taskId(3L)
                .title("Fix login bug")
                .description("Bug in login system")
                .status(Task.Status.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(bugTask));

        // Act
        List<Task> result = taskService.filterTasks(filterDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().toLowerCase().contains("bug"));
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void testFilterTasks_MultipleFilters() {
        // Arrange
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setStatus(Task.Status.OPEN);
        filterDTO.setProjectId(1L);
        filterDTO.setSearchKeyword("feature");
        filterDTO.setSortBy("dueDate");
        filterDTO.setSortDirection("ASC");

        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = taskService.filterTasks(filterDTO);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void testFilterTasks_ByDateRange() {
        // Arrange
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setDueDateFrom(LocalDateTime.now());
        filterDTO.setDueDateTo(LocalDateTime.now().plusDays(14));

        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = taskService.filterTasks(filterDTO);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void testFilterTasks_Overdue() {
        // Arrange
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setOverdue(true);

        Task overdueTask = Task.builder()
                .taskId(4L)
                .title("Overdue Task")
                .status(Task.Status.OPEN)
                .dueDate(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();

        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(overdueTask));

        // Act
        List<Task> result = taskService.filterTasks(filterDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void testFilterTasks_Unassigned() {
        // Arrange
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setUnassigned(true);

        Task unassignedTask = Task.builder()
                .taskId(5L)
                .title("Unassigned Task")
                .status(Task.Status.OPEN)
                .assignee(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(unassignedTask));

        // Act
        List<Task> result = taskService.filterTasks(filterDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getAssignee());
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void testSearchTasks_Success() {
        // Arrange
        String keyword = "bug";
        when(taskRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = taskService.searchTasks(keyword);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testGetOverdueTasks_Success() {
        // Arrange
        Task overdueTask = Task.builder()
                .taskId(6L)
                .title("Overdue")
                .status(Task.Status.OPEN)
                .dueDate(LocalDateTime.now().minusDays(1))
                .build();

        when(taskRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(overdueTask));

        // Act
        List<Task> result = taskService.getOverdueTasks();

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testGetUnassignedTasks_Success() {
        // Arrange
        Task unassignedTask = Task.builder()
                .taskId(7L)
                .title("Unassigned")
                .assignee(null)
                .build();

        when(taskRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(unassignedTask));

        // Act
        List<Task> result = taskService.getUnassignedTasks();

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testGetTaskById_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act
        Optional<Task> result = taskService.getTaskById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getTaskId());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTaskById_NotFound() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Task> result = taskService.getTaskById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testGetTasksAssignedToUser_Success() {
        // Arrange
        when(userRepository.findByUsername("assignee@example.com"))
                .thenReturn(Optional.of(assigneeUser));
        when(taskRepository.findByAssignee(assigneeUser))
                .thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = taskService.getTasksAssignedToUser("assignee@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByAssignee(assigneeUser);
    }

    @Test
    void testFilterTasks_WithSorting_Ascending() {
        // Arrange
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setSortBy("title");
        filterDTO.setSortDirection("ASC");

        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = taskService.filterTasks(filterDTO);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void testFilterTasks_WithSorting_Descending() {
        // Arrange
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setSortBy("createdAt");
        filterDTO.setSortDirection("DESC");

        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = taskService.filterTasks(filterDTO);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void testFilterTasks_DefaultSorting() {
        // Arrange
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        // No sorting specified - should use default

        when(taskRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = taskService.filterTasks(filterDTO);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }
}
