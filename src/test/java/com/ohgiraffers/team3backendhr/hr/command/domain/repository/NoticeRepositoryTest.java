package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.NoticeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ohgiraffers.team3backendhr.config.TestAuditConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestAuditConfig.class)
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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

    @Test
    @DisplayName("예약 공지 중 publishStartAt 이 기준 시각 이하인 것만 반환된다")
    void findByNoticeStatusAndPublishStartAtLessThanEqual_returnsOnlyPast() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(5);
        LocalDateTime future = LocalDateTime.now().plusHours(1);

        Notice pastReservation = Notice.builder()
                .noticeId(idGenerator.generate())
                .employeeId(employeeId)
                .noticeStatus(NoticeStatus.RESERVATION)
                .noticeTitle("과거 예약")
                .noticeContent("내용")
                .publishStartAt(past)
                .build();
        Notice futureReservation = Notice.builder()
                .noticeId(idGenerator.generate())
                .employeeId(employeeId)
                .noticeStatus(NoticeStatus.RESERVATION)
                .noticeTitle("미래 예약")
                .noticeContent("내용")
                .publishStartAt(future)
                .build();
        noticeRepository.save(pastReservation);
        noticeRepository.save(futureReservation);

        List<Notice> result = noticeRepository
                .findByNoticeStatusAndPublishStartAtLessThanEqual(NoticeStatus.RESERVATION, LocalDateTime.now());

        assertTrue(result.stream().allMatch(n -> n.getNoticeStatus() == NoticeStatus.RESERVATION));
        assertTrue(result.stream().anyMatch(n -> n.getNoticeTitle().equals("과거 예약")));
        assertTrue(result.stream().noneMatch(n -> n.getNoticeTitle().equals("미래 예약")));
    }

    @Test
    @DisplayName("isImportant=1 이고 importantEndAt 이 기준 시각 이전인 공지만 반환된다")
    void findByIsImportantAndImportantEndAtLessThan_returnsOnlyExpired() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        LocalDateTime future = LocalDateTime.now().plusDays(1);

        Notice expiredImportant = Notice.builder()
                .noticeId(idGenerator.generate())
                .employeeId(employeeId)
                .noticeStatus(NoticeStatus.POSTING)
                .noticeTitle("만료된 중요 공지")
                .noticeContent("내용")
                .isImportant(1)
                .importantEndAt(past)
                .build();
        Notice activeImportant = Notice.builder()
                .noticeId(idGenerator.generate())
                .employeeId(employeeId)
                .noticeStatus(NoticeStatus.POSTING)
                .noticeTitle("활성 중요 공지")
                .noticeContent("내용")
                .isImportant(1)
                .importantEndAt(future)
                .build();
        noticeRepository.save(expiredImportant);
        noticeRepository.save(activeImportant);

        List<Notice> result = noticeRepository
                .findByIsImportantAndImportantEndAtLessThan(1, LocalDateTime.now());

        assertTrue(result.stream().anyMatch(n -> n.getNoticeTitle().equals("만료된 중요 공지")));
        assertTrue(result.stream().noneMatch(n -> n.getNoticeTitle().equals("활성 중요 공지")));
    }
}
