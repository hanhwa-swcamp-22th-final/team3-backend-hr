package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeStatus;
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
class NoticeRepositoryTest {

    @Autowired
    private NoticeRepository noticeRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private Notice notice;
    private Long noticeId;
    private final Long employeeId = 1001L;

    @BeforeEach
    void setUp() {
        noticeId = idGenerator.generate();
        notice = Notice.builder()
                .noticeId(noticeId)
                .employeeId(employeeId)
                .noticeStatus(NoticeStatus.POSTING)
                .noticeTitle("공지 제목")
                .noticeContent("공지 내용")
                .build();
    }

    @Test
    @DisplayName("Save notice success: notice is persisted")
    void save_success() {
        Notice saved = noticeRepository.save(notice);

        assertNotNull(saved);
        assertEquals(noticeId, saved.getNoticeId());
        assertEquals(NoticeStatus.POSTING, saved.getNoticeStatus());
        assertEquals(0L, saved.getNoticeViews());
    }

    @Test
    @DisplayName("Find notice by id success: return persisted notice")
    void findById_success() {
        noticeRepository.save(notice);

        Optional<Notice> result = noticeRepository.findById(noticeId);

        assertTrue(result.isPresent());
        assertEquals("공지 제목", result.get().getNoticeTitle());
    }

    @Test
    @DisplayName("Find notice by id failure: return empty when notice does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<Notice> result = noticeRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find notices by status: return only notices matching status")
    void findByNoticeStatus_success() {
        noticeRepository.save(notice);
        noticeRepository.save(Notice.builder()
                .noticeId(idGenerator.generate())
                .employeeId(employeeId)
                .noticeStatus(NoticeStatus.TEMPORARY)
                .noticeTitle("임시 공지")
                .noticeContent("임시 내용")
                .build());

        List<Notice> result = noticeRepository.findByNoticeStatus(NoticeStatus.POSTING);

        assertTrue(result.stream().allMatch(n -> n.getNoticeStatus() == NoticeStatus.POSTING));
    }

    @Test
    @DisplayName("Find notices by employee id: return notices written by the employee")
    void findByEmployeeId_success() {
        noticeRepository.save(notice);

        List<Notice> result = noticeRepository.findByEmployeeId(employeeId);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(n -> n.getEmployeeId().equals(employeeId)));
    }
}
