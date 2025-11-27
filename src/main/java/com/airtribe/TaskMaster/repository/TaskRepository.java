package com.airtribe.TaskMaster.repository;

import com.airtribe.TaskMaster.model.Task;
import com.airtribe.TaskMaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


/**
 * Repository interface for Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignee(User user);
    List<Task> findByProjectId(Long projectId);
}