package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeTarget;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeTargetRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ohgiraffers.team3backendhr.config.TestAuditConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestAuditConfig.class)
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoticeTargetRepositoryTest {

    @Autowired
    private NoticeTargetRepository noticeTargetRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private NoticeTarget noticeTarget;
    private Long noticeTargetId;
    private Long noticeId;

    @BeforeEach
    void setUp() {
        noticeId = idGenerator.generate();
        noticeRepository.save(Notice.builder()
                .noticeId(noticeId)
                .employeeId(1001L)
                .noticeStatus(NoticeStatus.POSTING)
                .noticeTitle("공지 제목")
                .noticeContent("공지 내용")
                .build());

        noticeTargetId = idGenerator.generate();
        noticeTarget = NoticeTarget.builder()
                .noticeTargetId(noticeTargetId)
                .noticeId(noticeId)
                .targetRole(NoticeTargetRole.WORKER)
                .build();
    }

    @Test
    @DisplayName("Save notice target success: notice target is persisted")
    void save_success() {
        NoticeTarget saved = noticeTargetRepository.save(noticeTarget);

        assertNotNull(saved);
        assertEquals(noticeTargetId, saved.getNoticeTargetId());
        assertEquals(NoticeTargetRole.WORKER, saved.getTargetRole());
    }

    @Test
    @DisplayName("Find notice target by id success: return persisted notice target")
    void findById_success() {
        noticeTargetRepository.save(noticeTarget);

        Optional<NoticeTarget> result = noticeTargetRepository.findById(noticeTargetId);

        assertTrue(result.isPresent());
        assertEquals(noticeId, result.get().getNoticeId());
    }

    @Test
    @DisplayName("Find notice target by id failure: return empty when notice target does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<NoticeTarget> result = noticeTargetRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find notice targets by notice id: return all targets of the notice")
    void findByNoticeId_success() {
        noticeTargetRepository.save(noticeTarget);
        noticeTargetRepository.save(NoticeTarget.builder()
                .noticeTargetId(idGenerator.generate())
                .noticeId(noticeId)
                .targetRole(NoticeTargetRole.TL)
                .build());

        List<NoticeTarget> result = noticeTargetRepository.findByNoticeId(noticeId);

        assertTrue(result.size() >= 2);
        assertTrue(result.stream().allMatch(t -> t.getNoticeId().equals(noticeId)));
    }
}
