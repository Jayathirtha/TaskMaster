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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"task", "author", "attachments"})
@ToString(exclude = {"task", "author", "attachments"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler","task"})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    private String content;
    
    @Deprecated // Deprecated in favor of attachments list
    private String attachmentFileName; // Legacy field for backward compatibility
    
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationship: Many-to-One with Task
    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    // Relationship: Many-to-One with User (Author)
    @ManyToOne(fetch = FetchType.LAZY)
    private User author;
    
    // Relationship: One-to-Many with Attachment (NEW)
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

}