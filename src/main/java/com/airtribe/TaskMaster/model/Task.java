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
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"project", "assignee", "comments"})
@ToString(exclude = {"project", "assignee", "comments"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler","assignee"})
public class Task {

    public enum Status {
        OPEN, IN_PROGRESS, COMPLETE, ARCHIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    private String title;
    private String description;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.OPEN;

    // Relationship: Many-to-One with Project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_project",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Project project;

    // Relationship: Many-to-One with User (Assignee)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_assignee",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private User assignee;

    // Relationship: One-to-Many with Comment
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();
}