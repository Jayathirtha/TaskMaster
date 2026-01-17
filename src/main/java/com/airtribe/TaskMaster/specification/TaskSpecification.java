package com.airtribe.TaskMaster.specification;

import com.airtribe.TaskMaster.model.Task;
import com.airtribe.TaskMaster.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification builder for dynamic Task queries.
 * Enables complex filtering and searching using Spring Data JPA Specifications.
 */
public class TaskSpecification {

    /**
     * Build a specification based on multiple filter criteria.
     */
    public static Specification<Task> filterTasks(
            Task.Status status,
            String searchKeyword,
            Long projectId,
            Long assigneeId,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            LocalDateTime createdFrom,
            LocalDateTime createdTo) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Search in title or description
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String likePattern = "%" + searchKeyword.toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), likePattern);
                Predicate descriptionMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), likePattern);
                predicates.add(criteriaBuilder.or(titleMatch, descriptionMatch));
            }

            // Filter by project
            if (projectId != null) {
                predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));
            }

            // Filter by assignee
            if (assigneeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignee").get("userId"), assigneeId));
            }

            // Filter by due date range
            if (dueDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
            }
            if (dueDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
            }

            // Filter by creation date range
            if (createdFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter tasks by status only.
     */
    public static Specification<Task> hasStatus(Task.Status status) {
        return (root, query, criteriaBuilder) -> 
            status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * Search tasks by keyword in title or description.
     */
    public static Specification<Task> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return null;
            }
            String likePattern = "%" + keyword.toLowerCase() + "%";
            Predicate titleMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), likePattern);
            Predicate descriptionMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), likePattern);
            return criteriaBuilder.or(titleMatch, descriptionMatch);
        };
    }

    /**
     * Filter tasks by project ID.
     */
    public static Specification<Task> belongsToProject(Long projectId) {
        return (root, query, criteriaBuilder) -> 
            projectId == null ? null : criteriaBuilder.equal(root.get("project").get("id"), projectId);
    }

    /**
     * Filter tasks by assignee username.
     */
    public static Specification<Task> assignedToUser(String username) {
        return (root, query, criteriaBuilder) -> {
            if (username == null || username.trim().isEmpty()) {
                return null;
            }
            Join<Task, User> assigneeJoin = root.join("assignee");
            return criteriaBuilder.equal(assigneeJoin.get("username"), username);
        };
    }

    /**
     * Filter tasks by due date range.
     */
    public static Specification<Task> dueDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return null;
            }
            if (from != null && to != null) {
                return criteriaBuilder.between(root.get("dueDate"), from, to);
            }
            if (from != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), from);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), to);
        };
    }

    /**
     * Filter tasks that are overdue (past due date and not completed).
     */
    public static Specification<Task> isOverdue() {
        return (root, query, criteriaBuilder) -> {
            LocalDateTime now = LocalDateTime.now();
            Predicate pastDue = criteriaBuilder.lessThan(root.get("dueDate"), now);
            Predicate notCompleted = criteriaBuilder.notEqual(root.get("status"), Task.Status.COMPLETE);
            Predicate notArchived = criteriaBuilder.notEqual(root.get("status"), Task.Status.ARCHIVED);
            return criteriaBuilder.and(pastDue, notCompleted, notArchived);
        };
    }

    /**
     * Filter tasks without an assignee (unassigned tasks).
     */
    public static Specification<Task> isUnassigned() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.isNull(root.get("assignee"));
    }
}
