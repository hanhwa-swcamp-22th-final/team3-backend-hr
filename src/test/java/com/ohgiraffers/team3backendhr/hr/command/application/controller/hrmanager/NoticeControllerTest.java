package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticePublishRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeScheduleRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.NoticeCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.NoticeStatus;
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
import static org.mockito.BDDMockito.given;
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
                List.of(new SimpleGrantedAuthority("HRM")));
    }

    @Test
    @DisplayName("공지 즉시 게시 성공 — 201 Created")
    void publishNotice_success() throws Exception {
        NoticePublishRequest request = new NoticePublishRequest(
                "전사 공지", "공지 내용입니다.",
                true,
                LocalDateTime.of(2026, 4, 30, 23, 59)
        );

        mockMvc.perform(post("/api/v1/hr/notices")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(noticeCommandService).publishNotice(any(NoticePublishRequest.class), eq(99L));
    }

    @Test
    @DisplayName("공지 즉시 게시 — 제목 누락 시 400")
    void publishNotice_fail_missingTitle() throws Exception {
        NoticePublishRequest request = new NoticePublishRequest(
                "", "공지 내용입니다.", false, null
        );

        mockMvc.perform(post("/api/v1/hr/notices")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공지 예약 게시 성공 — 201 Created")
    void scheduleNotice_success() throws Exception {
        NoticeScheduleRequest request = new NoticeScheduleRequest(
                "전사 공지", "공지 내용입니다.",
                false,
                null,
                LocalDateTime.of(2099, 5, 1, 9, 0)
        );

        mockMvc.perform(post("/api/v1/hr/notices/schedule")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(noticeCommandService).scheduleNotice(any(NoticeScheduleRequest.class), eq(99L));
    }

    @Test
    @DisplayName("공지 예약 게시 — 예약 시각 누락 시 400")
    void scheduleNotice_fail_missingPublishStartAt() throws Exception {
        NoticeScheduleRequest request = new NoticeScheduleRequest(
                "전사 공지", "공지 내용입니다.", false, null, null
        );

        mockMvc.perform(post("/api/v1/hr/notices/schedule")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공지 임시 저장 성공 — 201 Created, noticeId 반환")
    void draftNotice_success() throws Exception {
        NoticeDraftRequest request = new NoticeDraftRequest(null, null, null, false, null);
        given(noticeCommandService.draftNotice(any(NoticeDraftRequest.class), eq(99L))).willReturn(1000L);

        mockMvc.perform(post("/api/v1/hr/notices/draft")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1000));

        verify(noticeCommandService).draftNotice(any(NoticeDraftRequest.class), eq(99L));
    }

    @Test
    @DisplayName("공지 수정 성공 — 200 OK (PUT)")
    void updateNotice_success() throws Exception {
        NoticeUpdateRequest request = new NoticeUpdateRequest(
                NoticeStatus.POSTING, false,
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
                NoticeStatus.POSTING, false, "제목", "내용", null, null
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
