package com.airtribe.TaskMaster.repository;

import com.airtribe.TaskMaster.model.Attachment;
import com.airtribe.TaskMaster.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Attachment entity operations.
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    
    /**
     * Find all attachments for a specific comment.
     */
    List<Attachment> findByComment(Comment comment);
    
    /**
     * Find all attachments for a comment by comment ID.
     */
    List<Attachment> findByComment_CommentId(Long commentId);
}
