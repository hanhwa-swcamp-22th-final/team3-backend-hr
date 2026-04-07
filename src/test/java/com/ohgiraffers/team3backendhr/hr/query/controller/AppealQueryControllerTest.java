package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.AppealQueryService;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.jwt.JwtTokenProvider;
import com.ohgiraffers.team3backendhr.jwt.RestAccessDeniedHandler;
import com.ohgiraffers.team3backendhr.jwt.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppealQueryController.class)
class AppealQueryControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppealQueryService service;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    /* ── GET /appeals/{appealId} (HRM) ───────────────────────────── */

    @Test
    @DisplayName("이의신청 상세 조회 — HRM 성공 200 OK")
    void getAppeal_hrm_success() throws Exception {
        // given
        AppealDetailResponse detail = new AppealDetailResponse();
        detail.setAppealId(1L);
        given(service.getAppeal(anyLong(), any())).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/hr/appeals/1")
                        .with(user("hrm").authorities(new SimpleGrantedAuthority("HRM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이의신청 상세 조회 — Worker 성공 200 OK")
    void getAppeal_worker_success() throws Exception {
        // given
        AppealDetailResponse detail = new AppealDetailResponse();
        detail.setAppealId(1L);
        given(service.getAppeal(anyLong(), any())).willReturn(detail);

        EmployeeUserDetails workerDetails = new EmployeeUserDetails(
                100L, "W001", "pw", List.of(new SimpleGrantedAuthority("WORKER")));
        UsernamePasswordAuthenticationToken workerAuth =
                new UsernamePasswordAuthenticationToken(workerDetails, null, workerDetails.getAuthorities());

        // when & then
        mockMvc.perform(get("/api/v1/hr/appeals/1")
                        .with(authentication(workerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이의신청 상세 조회 — 권한 없으면 403")
    void getAppeal_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/hr/appeals/1")
                        .with(user("tl").roles("TL")))
                .andExpect(status().isForbidden());
    }

    /* ── GET /appeals (HRM) ───────────────────────────────────────── */

    @Test
    @DisplayName("이의신청 목록 조회 성공 — 200 OK")
    void getAppeals_success() throws Exception {
        // given
        given(service.getAppeals(any(), anyInt(), anyInt()))
                .willReturn(new AppealListResponse(List.of(), 0L, 0L));

        // when & then
        mockMvc.perform(get("/api/v1/hr/appeals")
                        .with(user("hrm").authorities(new SimpleGrantedAuthority("HRM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이의신청 목록 조회 — HRM 권한 없으면 403")
    void getAppeals_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/hr/appeals")
                        .with(user("worker").roles("WORKER")))
                .andExpect(status().isForbidden());
    }

    /* ── GET /appeals/me (Worker) ─────────────────────────────────── */

    @Test
    @DisplayName("내 이의신청 목록 조회 성공 — 200 OK")
    void getMyAppeals_success() throws Exception {
        // given
        AppealSummaryResponse item = new AppealSummaryResponse();
        item.setAppealId(1L);
        given(service.getMyAppeals(anyLong())).willReturn(List.of(item));

        EmployeeUserDetails workerDetails = new EmployeeUserDetails(
                100L, "W001", "pw", List.of(new SimpleGrantedAuthority("WORKER")));
        UsernamePasswordAuthenticationToken workerAuth =
                new UsernamePasswordAuthenticationToken(workerDetails, null, workerDetails.getAuthorities());

        // when & then
        mockMvc.perform(get("/api/v1/hr/appeals/me")
                        .with(authentication(workerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }


}
