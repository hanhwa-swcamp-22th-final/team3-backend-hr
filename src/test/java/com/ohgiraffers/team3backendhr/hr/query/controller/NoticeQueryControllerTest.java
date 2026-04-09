package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.command.application.service.NoticeCommandService;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticePinnedResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.NoticeQueryService;
import com.ohgiraffers.team3backendhr.jwt.JwtTokenProvider;
import com.ohgiraffers.team3backendhr.jwt.RestAccessDeniedHandler;
import com.ohgiraffers.team3backendhr.jwt.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoticeQueryController.class)
class NoticeQueryControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoticeQueryService noticeQueryService;

    @MockitoBean
    private NoticeCommandService noticeCommandService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private EmployeeUserDetails workerUser() {
        return new EmployeeUserDetails(1L, "EMP-WORKER", "pw",
                List.of(new SimpleGrantedAuthority("WORKER")));
    }

    private EmployeeUserDetails hrmUser() {
        return new EmployeeUserDetails(99L, "EMP-HRM", "pw",
                List.of(new SimpleGrantedAuthority("HRM")));
    }

    @Test
    @DisplayName("공지 목록 조회 — Worker는 status=POSTING 강제 적용")
    void getNotices_worker_forcesPostingStatus() throws Exception {
        given(noticeQueryService.getNotices(isNull(), isNull(), eq("POSTING"), eq(0), eq(20)))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/hr/notices")
                        .with(user(workerUser())))
                .andExpect(status().isOk());

        verify(noticeQueryService).getNotices(null, null, "POSTING", 0, 20);
    }

    @Test
    @DisplayName("공지 목록 조회 — Worker는 isImportant 필터 사용 가능")
    void getNotices_worker_canFilterByImportant() throws Exception {
        given(noticeQueryService.getNotices(isNull(), eq(true), eq("POSTING"), eq(0), eq(20)))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/hr/notices")
                        .param("isImportant", "true")
                        .with(user(workerUser())))
                .andExpect(status().isOk());

        verify(noticeQueryService).getNotices(null, true, "POSTING", 0, 20);
    }

    @Test
    @DisplayName("공지 목록 조회 — HRM은 status 파라미터 그대로 사용")
    void getNotices_hrm_usesStatusParam() throws Exception {
        given(noticeQueryService.getNotices(isNull(), isNull(), eq("TEMPORARY"), eq(0), eq(20)))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/hr/notices")
                        .param("status", "TEMPORARY")
                        .with(user(hrmUser())))
                .andExpect(status().isOk());

        verify(noticeQueryService).getNotices(null, null, "TEMPORARY", 0, 20);
    }

    @Test
    @DisplayName("공지 목록 조회 — 결과 200 OK")
    void getNotices_success() throws Exception {
        NoticeListResponse item = new NoticeListResponse(
                1L, "공지 제목", 99L, "POSTING", 0, 10L,
                LocalDateTime.of(2026, 4, 1, 9, 0), null, null);
        given(noticeQueryService.getNotices(any(), any(), any(), eq(0), eq(20)))
                .willReturn(List.of(item));

        mockMvc.perform(get("/api/v1/hr/notices")
                        .with(user(workerUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].noticeId").value(1));
    }

    @Test
    @DisplayName("고정 공지 조회 — 200 OK")
    void getPinnedNotice_success() throws Exception {
        NoticePinnedResponse response = new NoticePinnedResponse(
                1L, "중요 공지", "중요 내용",
                LocalDateTime.of(2026, 4, 1, 9, 0), null);
        given(noticeQueryService.getPinnedNotice()).willReturn(response);

        mockMvc.perform(get("/api/v1/hr/notices/pinned")
                        .with(user(workerUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noticeTitle").value("중요 공지"));
    }

    @Test
    @DisplayName("공지 상세 조회 — 200 OK 및 조회수 증가 호출")
    void getNoticeDetail_success() throws Exception {
        NoticeDetailResponse response = new NoticeDetailResponse(
                1L, "제목", "내용", 99L, "POSTING", 0, 5L,
                LocalDateTime.of(2026, 4, 1, 9, 0), null, null, null);
        given(noticeQueryService.getNoticeDetail(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/hr/notices/1")
                        .with(user(workerUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noticeId").value(1));

        verify(noticeCommandService).incrementViews(1L);
    }

    @Test
    @DisplayName("공지 상세 조회 — 존재하지 않으면 404")
    void getNoticeDetail_notFound() throws Exception {
        given(noticeQueryService.getNoticeDetail(9999L))
                .willThrow(new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/hr/notices/9999")
                        .with(user(workerUser())))
                .andExpect(status().isNotFound());
    }
}
