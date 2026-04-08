package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticePinnedResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class NoticeQueryMapperTest {

    @Autowired
    private NoticeQueryMapper mapper;

    @Autowired
    private NoticeRepository noticeRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();
    private final Long employeeId = 100L;

    private Long postingImportantId;
    private Long postingNormalId;
    private Long temporaryId;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        // 보안 컨텍스트 설정 (created_by NOT NULL 충족)
        EmployeeUserDetails user = new EmployeeUserDetails(employeeId, "EMP-TEST", "pw",
                List.of(new SimpleGrantedAuthority("HRM")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        // 즉시 게시 + 중요 공지
        postingImportantId = idGenerator.generate();
        noticeRepository.saveAndFlush(Notice.builder()
                .noticeId(postingImportantId)
                .employeeId(employeeId)
                .noticeStatus(NoticeStatus.POSTING)
                .isImportant(1)
                .noticeTitle("중요 공지사항")
                .noticeContent("중요 내용입니다.")
                .importantEndAt(LocalDateTime.of(2099, 12, 31, 23, 59))
                .build());

        // 즉시 게시 + 일반 공지
        postingNormalId = idGenerator.generate();
        noticeRepository.saveAndFlush(Notice.builder()
                .noticeId(postingNormalId)
                .employeeId(employeeId)
                .noticeStatus(NoticeStatus.POSTING)
                .isImportant(0)
                .noticeTitle("일반 공지사항")
                .noticeContent("일반 내용입니다.")
                .build());

        // 임시 저장
        temporaryId = idGenerator.generate();
        noticeRepository.saveAndFlush(Notice.builder()
                .noticeId(temporaryId)
                .employeeId(employeeId)
                .noticeStatus(NoticeStatus.TEMPORARY)
                .isImportant(0)
                .noticeTitle("임시 저장 공지")
                .noticeContent("임시 내용입니다.")
                .build());
    }

    @Test
    @DisplayName("공지 목록 조회 — 필터 없이 전체 반환")
    void findNotices_noFilter_returnsAll() {
        List<NoticeListResponse> result = mapper.findNotices(null, null, null, 0, 20);

        assertThat(result.stream().map(NoticeListResponse::getNoticeId).toList())
                .contains(postingImportantId, postingNormalId, temporaryId);
    }

    @Test
    @DisplayName("공지 목록 조회 — status 필터 적용")
    void findNotices_statusFilter_returnsFiltered() {
        List<NoticeListResponse> result = mapper.findNotices(null, "POSTING", null, 0, 20);

        assertThat(result).allMatch(r -> r.getNoticeStatus().equals("POSTING"));
        assertThat(result.stream().map(NoticeListResponse::getNoticeId).toList())
                .contains(postingImportantId, postingNormalId)
                .doesNotContain(temporaryId);
    }

    @Test
    @DisplayName("공지 목록 조회 — keyword 필터 적용")
    void findNotices_keywordFilter_returnsMatched() {
        List<NoticeListResponse> result = mapper.findNotices("중요", null, null, 0, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNoticeId()).isEqualTo(postingImportantId);
    }

    @Test
    @DisplayName("공지 목록 조회 — size 파라미터가 적용된다")
    void findNotices_sizeLimited() {
        List<NoticeListResponse> result = mapper.findNotices(null, null, null, 0, 1);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("공지 건수 조회 — 전체 건수 반환")
    void countNotices_noFilter_returnsTotal() {
        long count = mapper.countNotices(null, null, null);

        assertThat(count).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("공지 건수 조회 — status 필터 적용")
    void countNotices_statusFilter() {
        long count = mapper.countNotices(null, "TEMPORARY", null);

        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("공지 상세 조회 — 존재하는 ID로 조회 성공")
    void findById_success() {
        NoticeDetailResponse result = mapper.findById(postingNormalId);

        assertThat(result).isNotNull();
        assertThat(result.getNoticeId()).isEqualTo(postingNormalId);
        assertThat(result.getNoticeTitle()).isEqualTo("일반 공지사항");
        assertThat(result.getNoticeContent()).isEqualTo("일반 내용입니다.");
        assertThat(result.getAuthorId()).isEqualTo(employeeId);
    }

    @Test
    @DisplayName("공지 상세 조회 — 존재하지 않는 ID면 null 반환")
    void findById_notFound_returnsNull() {
        NoticeDetailResponse result = mapper.findById(9999999L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("고정 공지 조회 — is_important=1 이고 POSTING 상태인 공지를 반환한다")
    void findPinned_returnsMostRecentImportantPosting() {
        NoticePinnedResponse result = mapper.findPinned();

        assertThat(result).isNotNull();
        assertThat(result.getNoticeId()).isEqualTo(postingImportantId);
        assertThat(result.getNoticeTitle()).isEqualTo("중요 공지사항");
    }
}
