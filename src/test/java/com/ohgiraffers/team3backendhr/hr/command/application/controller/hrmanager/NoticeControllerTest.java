package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.NoticeCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeTargetRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoticeController.class)
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoticeCommandService noticeCommandService;

    private EmployeeUserDetails hrmUser() {
        return new EmployeeUserDetails(99L, "EMP-HRM", "password",
                List.of(new SimpleGrantedAuthority("ROLE_HRM")));
    }

    @Test
    @DisplayName("공지 생성 성공 — 201 Created")
    void createNotice_success() throws Exception {
        NoticeCreateRequest request = new NoticeCreateRequest(
                NoticeStatus.PUBLISHED, true,
                "전사 공지", "공지 내용입니다.",
                LocalDateTime.of(2026, 4, 1, 9, 0),
                LocalDateTime.of(2026, 4, 30, 23, 59),
                List.of(NoticeTargetRole.WORKER, NoticeTargetRole.TEAM_LEADER)
        );

        mockMvc.perform(post("/api/v1/hr/notices")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(noticeCommandService).createNotice(any(NoticeCreateRequest.class), eq(99L));
    }

    @Test
    @DisplayName("공지 생성 — 제목 누락 시 400")
    void createNotice_fail_missingTitle() throws Exception {
        NoticeCreateRequest request = new NoticeCreateRequest(
                NoticeStatus.PUBLISHED, false,
                "",
                "공지 내용입니다.",
                null, null,
                List.of(NoticeTargetRole.WORKER)
        );

        mockMvc.perform(post("/api/v1/hr/notices")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공지 수정 성공 — 200 OK (PUT)")
    void updateNotice_success() throws Exception {
        NoticeUpdateRequest request = new NoticeUpdateRequest(
                NoticeStatus.PUBLISHED, false,
                "수정된 제목", "수정된 내용",
                null, null
        );

        mockMvc.perform(put("/api/v1/hr/notices/1")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(noticeCommandService).updateNotice(eq(1L), any(NoticeUpdateRequest.class));
    }

    @Test
    @DisplayName("공지 수정 — 존재하지 않으면 404")
    void updateNotice_notFound() throws Exception {
        NoticeUpdateRequest request = new NoticeUpdateRequest(
                NoticeStatus.PUBLISHED, false, "제목", "내용", null, null
        );
        doThrow(new IllegalArgumentException("공지를 찾을 수 없습니다."))
                .when(noticeCommandService).updateNotice(any(), any());

        mockMvc.perform(put("/api/v1/hr/notices/9999")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("공지 삭제 성공 — 200 OK")
    void deleteNotice_success() throws Exception {
        mockMvc.perform(delete("/api/v1/hr/notices/1")
                        .with(csrf())
                        .with(user(hrmUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(noticeCommandService).deleteNotice(1L);
    }

    @Test
    @DisplayName("공지 삭제 — 존재하지 않으면 404")
    void deleteNotice_notFound() throws Exception {
        doThrow(new IllegalArgumentException("공지를 찾을 수 없습니다."))
                .when(noticeCommandService).deleteNotice(any());

        mockMvc.perform(delete("/api/v1/hr/notices/9999")
                        .with(csrf())
                        .with(user(hrmUser())))
                .andExpect(status().isNotFound());
    }
}
