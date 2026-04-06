package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.AttachmentFileGroup;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.ReferenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AttachmentFileGroupRepositoryTest {

    @Autowired
    private AttachmentFileGroupRepository attachmentFileGroupRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private AttachmentFileGroup fileGroup;
    private Long fileGroupId;
    private final Long referenceId = 2001L;

    @BeforeEach
    void setUp() {
        fileGroupId = idGenerator.generate();
        fileGroup = AttachmentFileGroup.builder()
                .fileGroupId(fileGroupId)
                .referenceId(referenceId)
                .referenceType(ReferenceType.NOTICE)
                .build();
    }

    @Test
    @DisplayName("Save attachment file group success: file group is persisted")
    void save_success() {
        AttachmentFileGroup saved = attachmentFileGroupRepository.save(fileGroup);

        assertNotNull(saved);
        assertEquals(fileGroupId, saved.getFileGroupId());
        assertEquals(ReferenceType.NOTICE, saved.getReferenceType());
    }

    @Test
    @DisplayName("Find file group by id success: return persisted file group")
    void findById_success() {
        attachmentFileGroupRepository.save(fileGroup);

        Optional<AttachmentFileGroup> result = attachmentFileGroupRepository.findById(fileGroupId);

        assertTrue(result.isPresent());
        assertEquals(referenceId, result.get().getReferenceId());
    }

    @Test
    @DisplayName("Find file group by id failure: return empty when file group does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<AttachmentFileGroup> result = attachmentFileGroupRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find file group by reference id and type: return matching file group")
    void findByReferenceIdAndReferenceType_success() {
        attachmentFileGroupRepository.save(fileGroup);

        Optional<AttachmentFileGroup> result = attachmentFileGroupRepository
                .findByReferenceIdAndReferenceType(referenceId, ReferenceType.NOTICE);

        assertTrue(result.isPresent());
        assertEquals(fileGroupId, result.get().getFileGroupId());
    }

    @Test
    @DisplayName("Find file groups by reference type: return all groups of given type")
    void findByReferenceType_success() {
        attachmentFileGroupRepository.save(fileGroup);
        attachmentFileGroupRepository.save(AttachmentFileGroup.builder()
                .fileGroupId(idGenerator.generate())
                .referenceId(idGenerator.generate())
                .referenceType(ReferenceType.EVALUATION)
                .build());

        List<AttachmentFileGroup> result = attachmentFileGroupRepository.findByReferenceType(ReferenceType.NOTICE);

        assertTrue(result.stream().allMatch(g -> g.getReferenceType() == ReferenceType.NOTICE));
    }
}
