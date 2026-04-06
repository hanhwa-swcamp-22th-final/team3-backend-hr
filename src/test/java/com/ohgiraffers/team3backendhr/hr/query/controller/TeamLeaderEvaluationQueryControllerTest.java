package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.TlEvaluationTargetResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.QualitativeEvaluationQueryService;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamLeaderEvaluationQueryController.class)
class TeamLeaderEvaluationQueryControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QualitativeEvaluationQueryService service;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private EmployeeUserDetails tlUser() {
        return new EmployeeUserDetails(200L, "TL001", "password",
                List.of(new SimpleGrantedAuthority("TL")));
    }

    /* ── GET /team-leader/evaluations/targets ─────────────────────────── */

    @Test
    @DisplayName("TL 평가 대상 조회 성공 — 200 OK")
    void getTargets_success() throws Exception {
        // given
        given(service.getTlTargets(any(), any()))
                .willReturn(new TlEvaluationTargetResponse(5L, List.of()));

        // when & then
        mockMvc.perform(get("/api/v1/hr/team-leader/evaluations/targets")
                        .with(user(tlUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("TL 평가 대상 조회 — TL 권한 없으면 403")
    void getTargets_forbidden() throws Exception {
        // given — WORKER 권한으로 요청

        // when & then
        mockMvc.perform(get("/api/v1/hr/team-leader/evaluations/targets")
                        .with(user("worker").roles("WORKER")))
                .andExpect(status().isForbidden());
    }
}
