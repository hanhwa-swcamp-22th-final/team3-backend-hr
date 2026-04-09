package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticePublishRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class NoticeCommandIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoticeRepository noticeRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();
    private static final Long HRM_EMPLOYEE_ID = 99L;

    private EmployeeUserDetails hrmUser() {
        return new EmployeeUserDetails(HRM_EMPLOYEE_ID, "EMP-HRM", "password",
                List.of(new SimpleGrantedAuthority("HRM")));
    }

    @BeforeEach
    void setUpSecurityContext() {
        EmployeeUserDetails user = hrmUser();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("공지 즉시 게시 전체 흐름 — 201 반환")
    void publishNotice_fullFlow() throws Exception {
        NoticePublishRequest request = new NoticePublishRequest(
                "전사 공지 제목", "전사 공지 내용입니다.",
                true,
                null
        );

        mockMvc.perform(post("/api/v1/hr/notices")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("공지 수정 전체 흐름 — 200 반환 및 DB에 변경사항이 반영된다")
    void updateNotice_fullFlow() throws Exception {
        Long noticeId = idGenerator.generate();
        noticeRepository.save(Notice.builder()
                .noticeId(noticeId)
                .employeeId(HRM_EMPLOYEE_ID)
                .noticeStatus(NoticeStatus.TEMPORARY)
                .noticeTitle("원래 제목")
                .noticeContent("원래 내용")
                .build());

        NoticeUpdateRequest request = new NoticeUpdateRequest(
                NoticeStatus.POSTING, false,
                "수정된 제목", "수정된 내용",
                null, null
        );

        mockMvc.perform(put("/api/v1/hr/notices/" + noticeId)
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Notice updated = noticeRepository.findById(noticeId).orElseThrow();
        assertThat(updated.getNoticeTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getNoticeContent()).isEqualTo("수정된 내용");
        assertThat(updated.getNoticeStatus()).isEqualTo(NoticeStatus.POSTING);
    }
}
