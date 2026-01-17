package com.airtribe.TaskMaster.DTO;

import lombok.Data;

/**
 * DTO for Comment creation.
 * 
 * Note: File attachments are handled separately via /api/attachments/upload endpoint.
 * After creating a comment, use the commentId to upload files.
 */
@Data
public class CommentDTO {
    private String content;
    
    @Deprecated // Use /api/attachments/upload endpoint instead
    private String attachmentFileName; // Legacy field for backward compatibility
    
    private Long taskId;
}