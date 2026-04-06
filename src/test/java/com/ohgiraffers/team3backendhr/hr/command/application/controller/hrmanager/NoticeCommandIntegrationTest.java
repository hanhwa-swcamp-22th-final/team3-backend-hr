package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeTargetRole;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeTargetRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NoticeCommandIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private NoticeTargetRepository noticeTargetRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();
    private static final Long HRM_EMPLOYEE_ID = 99L;

    private EmployeeUserDetails hrmUser() {
        return new EmployeeUserDetails(HRM_EMPLOYEE_ID, "EMP-HRM", "password",
                List.of(new SimpleGrantedAuthority("ROLE_HRM")));
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
    @DisplayName("공지 생성 전체 흐름 — 201 반환 및 DB에 공지·대상 역할이 저장된다")
    void createNotice_fullFlow() throws Exception {
        NoticeCreateRequest request = new NoticeCreateRequest(
                NoticeStatus.PUBLISHED, true,
                "전사 공지 제목", "전사 공지 내용입니다.",
                null, null,
                List.of(NoticeTargetRole.WORKER, NoticeTargetRole.TEAM_LEADER)
        );

        mockMvc.perform(post("/api/v1/hr/notices")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        List<Notice> saved = noticeRepository.findByEmployeeId(HRM_EMPLOYEE_ID);
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getNoticeTitle()).isEqualTo("전사 공지 제목");
        assertThat(saved.get(0).getIsImportant()).isEqualTo(1);

        Long savedNoticeId = saved.get(0).getNoticeId();
        assertThat(noticeTargetRepository.findByNoticeId(savedNoticeId)).hasSize(2);
    }

    @Test
    @DisplayName("공지 수정 전체 흐름 — 200 반환 및 DB에 변경사항이 반영된다")
    void updateNotice_fullFlow() throws Exception {
        Long noticeId = idGenerator.generate();
        noticeRepository.save(Notice.builder()
                .noticeId(noticeId)
                .employeeId(HRM_EMPLOYEE_ID)
                .noticeStatus(NoticeStatus.DRAFT)
                .noticeTitle("원래 제목")
                .noticeContent("원래 내용")
                .build());

        NoticeUpdateRequest request = new NoticeUpdateRequest(
                NoticeStatus.PUBLISHED, false,
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
        assertThat(updated.getNoticeStatus()).isEqualTo(NoticeStatus.PUBLISHED);
    }
}
