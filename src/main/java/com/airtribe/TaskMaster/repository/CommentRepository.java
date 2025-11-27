package com.airtribe.TaskMaster.repository;


import com.airtribe.TaskMaster.model.Comment;
import com.airtribe.TaskMaster.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTask(Task task);
}