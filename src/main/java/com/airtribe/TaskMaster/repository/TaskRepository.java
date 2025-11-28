package com.airtribe.TaskMaster.repository;

import com.airtribe.TaskMaster.model.Task;
import com.airtribe.TaskMaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;


/**
 * Repository interface for Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignee(User user);
    List<Task> findByProjectId(Long projectId);

    List<Task> findTaskByStatus(Task.Status status);

    @Query("SELECT t FROM Task t " +
            "WHERE (:searchTitle IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTitle, '%')))" +
            "OR  (:searchTitle IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTitle, '%'))) ")
    List<Task> findTaskByTitleOrDescription( @Param("searchTitle") String searchTitle);

}