package com.airtribe.TaskMaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"task", "author"})
@ToString(exclude = {"task", "author"})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    private String content;
    private String attachmentFileName; // Mock attachment metadata
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationship: Many-to-One with Task
    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    // Relationship: Many-to-One with User (Author)
    @ManyToOne(fetch = FetchType.LAZY)
    private User author;
}