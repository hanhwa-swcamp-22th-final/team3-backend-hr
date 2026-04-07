package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationperiod.EvaluationPeriodDeadlineResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationperiod.EvaluationPeriodListResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.EvaluationPeriodQueryService;
import com.ohgiraffers.team3backendhr.jwt.JwtTokenProvider;
import com.ohgiraffers.team3backendhr.jwt.RestAccessDeniedHandler;
import com.ohgiraffers.team3backendhr.jwt.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvaluationPeriodQueryController.class)
class EvaluationPeriodQueryControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluationPeriodQueryService service;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    /* ── GET /evaluation-periods ──────────────────────────────────────── */

    @Test
    @DisplayName("평가 기간 목록 조회 성공 — 200 OK")
    void getEvaluationPeriods_success() throws Exception {
        // given
        given(service.getEvaluationPeriods(null, null, 0, 10))
                .willReturn(new EvaluationPeriodListResponse(List.of(), 0L, 0L));

        // when & then
        mockMvc.perform(get("/api/v1/hr/evaluation-periods")
                        .with(user("hrm").authorities(new SimpleGrantedAuthority("HRM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("평가 기간 목록 조회 — HRM 권한 없으면 403")
    void getEvaluationPeriods_forbidden() throws Exception {
        // given — TL 권한으로 요청

        // when & then
        mockMvc.perform(get("/api/v1/hr/evaluation-periods")
                        .with(user("tl").roles("TL")))
                .andExpect(status().isForbidden());
    }

    /* ── GET /evaluation-periods/deadline ────────────────────────────── */

    @Test
    @DisplayName("마감일 조회 성공 — 200 OK")
    void getDeadline_success() throws Exception {
        // given
        EvaluationPeriodDeadlineResponse response = new EvaluationPeriodDeadlineResponse();
        response.setEvalPeriodId(5L);
        response.setDaysRemaining(7L);
        response.setEndDate(LocalDate.of(2026, 4, 10));
        given(service.getDeadline()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/hr/evaluation-periods/deadline")
                        .with(user("tl").authorities(new SimpleGrantedAuthority("TL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("마감일 조회 — WORKER 권한이면 403")
    void getDeadline_forbidden() throws Exception {
        // given — WORKER 권한으로 요청

        // when & then
        mockMvc.perform(get("/api/v1/hr/evaluation-periods/deadline")
                        .with(user("worker").roles("WORKER")))
                .andExpect(status().isForbidden());
    }
}
