package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Attachment;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.AttachmentFileGroup;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.FileType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.ReferenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AttachmentRepositoryTest {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private AttachmentFileGroupRepository attachmentFileGroupRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private Attachment attachment;
    private Long attachmentId;
    private Long fileGroupId;

    @BeforeEach
    void setUp() {
        fileGroupId = idGenerator.generate();
        attachmentFileGroupRepository.save(AttachmentFileGroup.builder()
                .fileGroupId(fileGroupId)
                .referenceId(1001L)
                .referenceType(ReferenceType.NOTICE)
                .build());

        attachmentId = idGenerator.generate();
        attachment = Attachment.builder()
                .attachmentId(attachmentId)
                .fileGroupId(fileGroupId)
                .fileName("공지사항.pdf")
                .filePath("/uploads/notice/공지사항.pdf")
                .fileSize(204800L)
                .fileType(FileType.PDF)
                .fileAttachmentUploadedAt(LocalDateTime.of(2026, 4, 1, 9, 0))
                .build();
    }

    @Test
    @DisplayName("Save attachment success: attachment is persisted")
    void save_success() {
        Attachment saved = attachmentRepository.save(attachment);

        assertNotNull(saved);
        assertEquals(attachmentId, saved.getAttachmentId());
        assertEquals(FileType.PDF, saved.getFileType());
        assertEquals("공지사항.pdf", saved.getFileName());
    }

    @Test
    @DisplayName("Find attachment by id success: return persisted attachment")
    void findById_success() {
        attachmentRepository.save(attachment);

        Optional<Attachment> result = attachmentRepository.findById(attachmentId);

        assertTrue(result.isPresent());
        assertEquals(fileGroupId, result.get().getFileGroupId());
    }

    @Test
    @DisplayName("Find attachment by id failure: return empty when attachment does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<Attachment> result = attachmentRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find attachments by file group id: return all attachments in the group")
    void findByFileGroupId_success() {
        attachmentRepository.save(attachment);
        attachmentRepository.save(Attachment.builder()
                .attachmentId(idGenerator.generate())
                .fileGroupId(fileGroupId)
                .fileName("첨부이미지.jpg")
                .filePath("/uploads/notice/첨부이미지.jpg")
                .fileSize(102400L)
                .fileType(FileType.JPG)
                .fileAttachmentUploadedAt(LocalDateTime.of(2026, 4, 1, 9, 5))
                .build());

        List<Attachment> result = attachmentRepository.findByFileGroupId(fileGroupId);

        assertTrue(result.size() >= 2);
        assertTrue(result.stream().allMatch(a -> a.getFileGroupId().equals(fileGroupId)));
    }
}
