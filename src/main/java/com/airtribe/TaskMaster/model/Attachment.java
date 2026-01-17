package com.airtribe.TaskMaster.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity for file attachments on comments.
 * Stores both metadata and actual file data as BLOB.
 */
@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"comment"})
@ToString(exclude = {"comment", "fileData"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "comment", "fileData"})
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attachmentId;

    private String fileName;
    private String fileType; // MIME type (e.g., application/pdf, image/png)
    private Long fileSize; // Size in bytes
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // Store actual file data as BLOB
    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;

    // Relationship: Many-to-One with Comment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
}
