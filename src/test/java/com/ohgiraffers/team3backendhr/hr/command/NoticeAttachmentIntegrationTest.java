package com.ohgiraffers.team3backendhr.hr.command;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.query.service.NoticeQueryService;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeDetailResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class NoticeAttachmentIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private NoticeRepository noticeRepository;
    @Autowired private NoticeQueryService noticeQueryService;
    @Autowired private IdGenerator idGenerator;
    @Autowired private jakarta.persistence.EntityManager entityManager;

    private static final Long HRM_ID = 99L;

    private EmployeeUserDetails hrmUser() {
        return new EmployeeUserDetails(HRM_ID, "EMP-HRM", "pw",
                List.of(new SimpleGrantedAuthority("HRM")));
    }

    @BeforeEach
    void setUp() {
        EmployeeUserDetails user = hrmUser();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("공지사항에 파일을 업로드하면 DB에 메타데이터가 저장되고 상세 조회 시 나타난다")
    void uploadAndCheckDetail_success() throws Exception {
        // 1. Given: 공지사항 생성
        Long noticeId = idGenerator.generate();
        Notice notice = Notice.builder()
                .noticeId(noticeId)
                .employeeId(HRM_ID)
                .noticeStatus(NoticeStatus.POSTING)
                .noticeTitle("파일 테스트 공지")
                .noticeContent("내용")
                .build();
        noticeRepository.save(notice);
        entityManager.flush();

        MockMultipartFile file1 = new MockMultipartFile("files", "test1.txt", "text/plain", "Hello".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.png", "image/png", "World".getBytes());

        // 2. When: 파일 업로드 API 호출
        mockMvc.perform(multipart("/api/v1/hr/notices/" + noticeId + "/attachments")
                        .file(file1).file(file2)
                        .with(csrf())
                        .with(user(hrmUser())))
                .andDo(print())
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        // 3. Then: Query 서비스를 통해 상세 조회 시 첨부파일이 포함되어 있는지 확인
        NoticeDetailResponse detail = noticeQueryService.getNoticeDetail(noticeId);

        assertThat(detail.getNoticeTitle()).isEqualTo("파일 테스트 공지");
        assertThat(detail.getAttachments()).isNotEmpty();
        assertThat(detail.getAttachments().size()).isGreaterThanOrEqualTo(1);
    }
}
