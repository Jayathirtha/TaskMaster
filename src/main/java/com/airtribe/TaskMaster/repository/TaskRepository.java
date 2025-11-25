package com.airtribe.TaskMaster.repository;

import com.airtribe.TaskMaster.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Finds a task by its ID and ensures it belongs to the specified user ID.
     */
    Optional<Task> findByTaskIdAndUserId(Long taskId, Long userId);


    /**
     * Retrieves all tasks for a specific user ID, supporting custom sorting.
     */
    Optional<List<Task>> findAllByUserId(Long userId);

    @Transactional
    void deleteTaskByTaskIdAndUserId(Long taskId, Long userId);
    /**
     * Custom query for filtering, sorting, and searching tasks based on criteria.
     * This uses SpEL to dynamically handle sorting and complex filtering.
     * NOTE: For complex, dynamic sorting/filtering, using Spring Data JPA Specifications
     * or QueryDSL would be cleaner, but this provides a simple solution.
     */
    @Query("SELECT t FROM Task t " +
            "WHERE t.userId = :userId " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:searchTitle IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTitle, '%'))) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'dueDateAsc' THEN t.dueDate END ASC, " +
            "CASE WHEN :sortBy = 'dueDateDesc' THEN t.dueDate END DESC, " +
            "t.createdAt DESC") // Default sort
    List<Task> findByCriteria(
            @Param("userId") Long userId,
            @Param("status") Task.Status status,
            @Param("searchTitle") String searchTitle,
            @Param("sortBy") String sortBy
    );
}