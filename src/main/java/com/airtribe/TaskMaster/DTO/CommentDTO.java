package com.airtribe.TaskMaster.DTO;

import lombok.Data;

/**
 * DTO for Comment creation.
 */
@Data
public class CommentDTO {
    private String content;
    private String attachmentFileName; // Mock: actual file handling simplified to metadata
    private Long taskId;
}