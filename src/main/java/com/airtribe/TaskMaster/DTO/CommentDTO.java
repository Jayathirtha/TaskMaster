package com.airtribe.TaskMaster.DTO;

import lombok.Data;

/**
 * DTO for Comment creation.
 */
@Data
public class CommentDTO {
    private String content;
    private String attachmentFileName; // Mock
    private Long taskId;
}