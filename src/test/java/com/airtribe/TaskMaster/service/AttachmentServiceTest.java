package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.model.Attachment;
import com.airtribe.TaskMaster.model.Comment;
import com.airtribe.TaskMaster.model.User;
import com.airtribe.TaskMaster.repository.AttachmentRepository;
import com.airtribe.TaskMaster.repository.CommentRepository;
import com.airtribe.TaskMaster.service.impl.AttachmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttachmentServiceImpl.
 * Tests file upload, download, deletion, and validation logic.
 */
@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    private Comment testComment;
    private User testUser;
    private Attachment testAttachment;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .userId(1L)
                .username("testuser@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        // Setup test comment
        testComment = Comment.builder()
                .commentId(1L)
                .content("Test comment")
                .author(testUser)
                .attachments(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        // Setup test attachment
        testAttachment = Attachment.builder()
                .attachmentId(1L)
                .fileName("test.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .fileData("test file content".getBytes())
                .comment(testComment)
                .uploadedAt(LocalDateTime.now())
                .build();

        // Setup test file
        testFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test file content".getBytes()
        );
    }

    @Test
    void testStoreAttachment_Success() throws IOException {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(testAttachment);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        Attachment result = attachmentService.storeAttachment(testFile, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("test.pdf", result.getFileName());
        assertEquals("application/pdf", result.getFileType());
        verify(attachmentRepository, times(1)).save(any(Attachment.class));
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testStoreAttachment_EmptyFile_ThrowsException() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.storeAttachment(emptyFile, 1L)
        );
        assertEquals("Cannot upload empty file", exception.getMessage());
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void testStoreAttachment_CommentNotFound_ThrowsException() {
        // Arrange
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.storeAttachment(testFile, 999L)
        );
        assertTrue(exception.getMessage().contains("Comment not found"));
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void testStoreAttachment_FileSizeExceeded_ThrowsException() {
        // Arrange
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                largeContent
        );
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.storeAttachment(largeFile, 1L)
        );
        assertTrue(exception.getMessage().contains("exceeds maximum allowed size"));
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void testGetAttachment_Success() {
        // Arrange
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));

        // Act
        Attachment result = attachmentService.getAttachment(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getAttachmentId());
        assertEquals("test.pdf", result.getFileName());
        verify(attachmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAttachment_NotFound_ThrowsException() {
        // Arrange
        when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.getAttachment(999L)
        );
        assertTrue(exception.getMessage().contains("Attachment not found"));
    }

    @Test
    void testGetAttachmentsByComment_Success() {
        // Arrange
        List<Attachment> attachments = Arrays.asList(testAttachment);
        when(attachmentRepository.findByComment_CommentId(1L)).thenReturn(attachments);

        // Act
        List<Attachment> result = attachmentService.getAttachmentsByComment(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test.pdf", result.get(0).getFileName());
        verify(attachmentRepository, times(1)).findByComment_CommentId(1L);
    }

    @Test
    void testGetAttachmentsByComment_EmptyList() {
        // Arrange
        when(attachmentRepository.findByComment_CommentId(1L)).thenReturn(new ArrayList<>());

        // Act
        List<Attachment> result = attachmentService.getAttachmentsByComment(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteAttachment_Success() {
        // Arrange
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));
        testComment.getAttachments().add(testAttachment);
        doNothing().when(attachmentRepository).delete(any(Attachment.class));

        // Act
        attachmentService.deleteAttachment(1L, "testuser@example.com");

        // Assert
        verify(attachmentRepository, times(1)).findById(1L);
        verify(attachmentRepository, times(1)).delete(testAttachment);
    }

    @Test
    void testDeleteAttachment_NotFound_ThrowsException() {
        // Arrange
        when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.deleteAttachment(999L, "testuser@example.com")
        );
        assertTrue(exception.getMessage().contains("Attachment not found"));
        verify(attachmentRepository, never()).delete(any(Attachment.class));
    }

    @Test
    void testDeleteAttachment_UnauthorizedUser_ThrowsException() {
        // Arrange
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));

        // Act & Assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> attachmentService.deleteAttachment(1L, "otheruser@example.com")
        );
        assertTrue(exception.getMessage().contains("don't have permission"));
        verify(attachmentRepository, never()).delete(any(Attachment.class));
    }

    @Test
    void testStoreAttachment_VariousFileTypes() throws IOException {
        // Test multiple file types
        String[] fileTypes = {"image/png", "image/jpeg", "application/msword", "text/plain"};
        String[] fileNames = {"image.png", "photo.jpg", "document.doc", "notes.txt"};

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        for (int i = 0; i < fileTypes.length; i++) {
            MultipartFile file = new MockMultipartFile(
                    "file",
                    fileNames[i],
                    fileTypes[i],
                    "content".getBytes()
            );

            Attachment attachment = Attachment.builder()
                    .attachmentId((long) i + 1)
                    .fileName(fileNames[i])
                    .fileType(fileTypes[i])
                    .fileSize((long) "content".getBytes().length)
                    .fileData("content".getBytes())
                    .comment(testComment)
                    .build();

            when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);

            Attachment result = attachmentService.storeAttachment(file, 1L);

            assertNotNull(result);
            assertEquals(fileNames[i], result.getFileName());
            assertEquals(fileTypes[i], result.getFileType());
        }

        verify(attachmentRepository, times(fileTypes.length)).save(any(Attachment.class));
    }
}
