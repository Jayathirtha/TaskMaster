package com.airtribe.TaskMaster.service.impl;

import com.airtribe.TaskMaster.model.Attachment;
import com.airtribe.TaskMaster.model.Comment;
import com.airtribe.TaskMaster.repository.AttachmentRepository;
import com.airtribe.TaskMaster.repository.CommentRepository;
import com.airtribe.TaskMaster.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for Attachment and File Management. (SRP: File Operations)
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    @Transactional
    public Attachment storeAttachment(MultipartFile file, Long commentId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        // Find the comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        // Validate file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }

        // Create attachment entity
        Attachment attachment = Attachment.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .fileData(file.getBytes())
                .uploadedAt(LocalDateTime.now())
                .comment(comment)
                .build();

        // Save attachment
        Attachment savedAttachment = attachmentRepository.save(attachment);

        // Add to comment's attachment list
        comment.getAttachments().add(savedAttachment);
        commentRepository.save(comment);

        return savedAttachment;
    }

    @Override
    public Attachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + attachmentId));
    }

    @Override
    public List<Attachment> getAttachmentsByComment(Long commentId) {
        return attachmentRepository.findByComment_CommentId(commentId);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, String username) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + attachmentId));

        // Check if user is the comment author or has team access
        Comment comment = attachment.getComment();
        String authorUsername = comment.getAuthor().getUsername();
        
        if (!authorUsername.equals(username)) {
            // Additional authorization check could be added here to verify team membership
            throw new SecurityException("You don't have permission to delete this attachment");
        }

        // Remove from comment's list first
        comment.getAttachments().remove(attachment);
        
        // Delete the attachment
        attachmentRepository.delete(attachment);
    }
}
