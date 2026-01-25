package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.model.Attachment;
import com.airtribe.TaskMaster.model.User;
import com.airtribe.TaskMaster.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * REST Controller for Attachment Management.
 * Handles file upload, download, and deletion for task comments.
 */
@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated.");
        }
        return ((User) Objects.requireNonNull(authentication.getPrincipal())).getUsername();
    }

    /**
     * Upload a file attachment to a comment.
     * POST /api/attachments/upload?commentId={commentId}
     * Content-Type: multipart/form-data
     * 
     * @param file The file to upload (multipart)
     * @param commentId The ID of the comment to attach the file to
     * @return Response with attachment details
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("commentId") Long commentId) {
        try {
            Attachment attachment = attachmentService.storeAttachment(file, commentId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "File uploaded successfully",
                    "attachmentId", attachment.getAttachmentId(),
                    "fileName", attachment.getFileName(),
                    "fileSize", attachment.getFileSize(),
                    "fileType", attachment.getFileType(),
                    "commentId", commentId
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Download a file attachment by its ID.
     * GET /api/attachments/{attachmentId}/download
     * 
     * @param attachmentId The ID of the attachment to download
     * @return The file as a downloadable resource
     */
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<?> downloadAttachment(@PathVariable Long attachmentId) {
        try {
            Attachment attachment = attachmentService.getAttachment(attachmentId);
            
            ByteArrayResource resource = new ByteArrayResource(attachment.getFileData());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .contentLength(attachment.getFileSize())
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get metadata for a specific attachment (without file data).
     * GET /api/attachments/{attachmentId}
     * 
     * @param attachmentId The ID of the attachment
     * @return Attachment metadata
     */
    @GetMapping("/{attachmentId}")
    public ResponseEntity<?> getAttachmentMetadata(@PathVariable Long attachmentId) {
        try {
            Attachment attachment = attachmentService.getAttachment(attachmentId);
            
            return ResponseEntity.ok(Map.of(
                    "attachmentId", attachment.getAttachmentId(),
                    "fileName", attachment.getFileName(),
                    "fileType", attachment.getFileType(),
                    "fileSize", attachment.getFileSize(),
                    "uploadedAt", attachment.getUploadedAt(),
                    "commentId", attachment.getComment().getCommentId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all attachments for a specific comment.
     * GET /api/attachments/comment/{commentId}
     * 
     * @param commentId The ID of the comment
     * @return List of attachment metadata
     */
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<?> getAttachmentsByComment(@PathVariable Long commentId) {
        try {
            List<Attachment> attachments = attachmentService.getAttachmentsByComment(commentId);
            
            // Return metadata only (without file data)
            List<Map<String, Object>> attachmentMetadata = attachments.stream()
                    .map(att -> Map.of(
                            "attachmentId", (Object) att.getAttachmentId(),
                            "fileName", att.getFileName(),
                            "fileType", att.getFileType(),
                            "fileSize", att.getFileSize(),
                            "uploadedAt", att.getUploadedAt()
                    ))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(attachmentMetadata);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete an attachment.
     * DELETE /api/attachments/{attachmentId}
     * 
     * @param attachmentId The ID of the attachment to delete
     * @return Success message
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            attachmentService.deleteAttachment(attachmentId, getCurrentUsername());
            return ResponseEntity.ok(Map.of(
                    "message", "Attachment deleted successfully",
                    "attachmentId", attachmentId
            ));
        } catch (SecurityException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
