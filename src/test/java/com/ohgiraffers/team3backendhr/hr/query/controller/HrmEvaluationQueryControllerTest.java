package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationListResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.HrmEvaluationQueryService;
import com.ohgiraffers.team3backendhr.hr.query.service.QualitativeEvaluationQueryService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HrmEvaluationQueryController.class)
class HrmEvaluationQueryControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QualitativeEvaluationQueryService qualService;

    @MockitoBean
    private HrmEvaluationQueryService hrmService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private EmployeeUserDetails hrmUser() {
        return new EmployeeUserDetails(1L, "HRM001", "password",
                List.of(new SimpleGrantedAuthority("HRM")));
    }

    /* ── GET /evaluations ────────────────────────────────────────────── */

    @Test
    @DisplayName("HRM 평가 목록 조회 성공 — 200 OK")
    void getEvaluations_success() throws Exception {
        given(qualService.getEvaluations(any(), any(), any(), anyInt(), anyInt()))
                .willReturn(new EvaluationListResponse(List.of(), 0L, 0L));

        mockMvc.perform(get("/api/v1/hr/evaluations").with(user(hrmUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("HRM 평가 목록 조회 — HRM 권한 없으면 403")
    void getEvaluations_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/hr/evaluations").with(user("worker").roles("WORKER")))
                .andExpect(status().isForbidden());
    }

    /* ── GET /evaluations/{evalId} ───────────────────────────────────── */

    @Test
    @DisplayName("HRM 평가 상세 조회 성공 — 200 OK")
    void getEvaluation_success() throws Exception {
        EvaluationDetailResponse detail = new EvaluationDetailResponse();
        detail.setEvalId(1L);
        given(qualService.getEvaluationDetail(anyLong())).willReturn(detail);

        mockMvc.perform(get("/api/v1/hr/evaluations/1").with(user(hrmUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /* ── GET /evaluations/summary ────────────────────────────────────── */

    @Test
    @DisplayName("HRM 등급별 집계 조회 성공 — 200 OK")
    void getEvaluationSummary_success() throws Exception {
        given(qualService.getEvaluationGradeSummary(any())).willReturn(List.of());

        mockMvc.perform(get("/api/v1/hr/evaluations/summary").with(user(hrmUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /* ── GET /evaluations/bias-report ────────────────────────────────── */

    @Test
    @DisplayName("HRM 편향 보정 이력 조회 성공 — 200 OK")
    void getBiasReport_success() throws Exception {
        given(hrmService.getBiasReport()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/hr/evaluations/bias-report").with(user(hrmUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /* ── GET /evaluations/anti-gaming-flags ──────────────────────────── */

    @Test
    @DisplayName("HRM 어뷰징 감지 목록 조회 성공 — 200 OK")
    void getAntiGamingFlags_success() throws Exception {
        given(hrmService.getAntiGamingFlags()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/hr/evaluations/anti-gaming-flags").with(user(hrmUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
