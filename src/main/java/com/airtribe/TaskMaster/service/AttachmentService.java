package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.model.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Interface for Attachment and File Management operations.
 */
public interface AttachmentService {
    
    /**
     * Store a file and create an attachment record linked to a comment.
     * 
     * @param file The multipart file to upload
     * @param commentId The ID of the comment this attachment belongs to
     * @return The created Attachment entity
     * @throws IOException If file processing fails
     * @throws IllegalArgumentException If comment not found
     */
    Attachment storeAttachment(MultipartFile file, Long commentId) throws IOException;
    
    /**
     * Retrieve an attachment by its ID.
     * 
     * @param attachmentId The ID of the attachment
     * @return The Attachment entity with file data
     * @throws IllegalArgumentException If attachment not found
     */
    Attachment getAttachment(Long attachmentId);
    
    /**
     * Get all attachments for a specific comment.
     * 
     * @param commentId The ID of the comment
     * @return List of attachments (without file data for performance)
     */
    List<Attachment> getAttachmentsByComment(Long commentId);
    
    /**
     * Delete an attachment by its ID.
     * 
     * @param attachmentId The ID of the attachment to delete
     * @param username The username of the user attempting deletion
     * @throws IllegalArgumentException If attachment not found
     * @throws SecurityException If user doesn't have permission
     */
    void deleteAttachment(Long attachmentId, String username);
}
