package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.query.dto.MissionResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerTierHistoryItem;
import com.ohgiraffers.team3backendhr.hr.query.service.MissionQueryService;
import com.ohgiraffers.team3backendhr.hr.query.service.PerformancePointQueryService;
import com.ohgiraffers.team3backendhr.hr.query.service.PromotionQueryService;
import com.ohgiraffers.team3backendhr.hr.query.service.WorkerEvaluationQueryService;
import com.ohgiraffers.team3backendhr.hr.query.service.WorkerProfileQueryService;
import com.ohgiraffers.team3backendhr.jwt.JwtTokenProvider;
import com.ohgiraffers.team3backendhr.jwt.RestAccessDeniedHandler;
import com.ohgiraffers.team3backendhr.jwt.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkerQueryController.class)
class WorkerQueryControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MissionQueryService missionQueryService;

    @MockitoBean
    private PromotionQueryService promotionQueryService;

    @MockitoBean
    private WorkerProfileQueryService workerProfileQueryService;

    @MockitoBean
    private PerformancePointQueryService performancePointQueryService;

    @MockitoBean
    private WorkerEvaluationQueryService workerEvaluationQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private EmployeeUserDetails workerUserDetails() {
        return new EmployeeUserDetails(1L, "EMP-WORKER", "pw",
                List.of(new SimpleGrantedAuthority("WORKER")));
    }

    private MissionResponse sampleMission(Long id, String status) {
        return new MissionResponse(id, 10L, "미션명", "HIGH_DIFFICULTY_WORK", "B",
                BigDecimal.valueOf(5), BigDecimal.valueOf(10), 50, status, 200, null);
    }

    @Test
    @DisplayName("전체 미션 목록 조회 — 200 OK")
    void getMissions_success() throws Exception {
        given(missionQueryService.getMissions(any(), eq(null), eq(0), eq(20)))
                .willReturn(List.of(sampleMission(1L, "IN_PROGRESS")));

        mockMvc.perform(get("/api/v1/hr/workers/me/missions")
                        .with(user(workerUserDetails())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].missionProgressId").value(1));
    }

    @Test
    @DisplayName("전체 미션 목록 조회 — status 파라미터 전달")
    void getMissions_withStatusFilter() throws Exception {
        given(missionQueryService.getMissions(any(), eq("COMPLETED"), eq(0), eq(20)))
                .willReturn(List.of(sampleMission(2L, "COMPLETED")));

        mockMvc.perform(get("/api/v1/hr/workers/me/missions")
                        .param("status", "COMPLETED")
                        .with(user(workerUserDetails())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("전체 미션 목록 조회 — 미션 없으면 빈 배열 반환")
    void getMissions_empty() throws Exception {
        given(missionQueryService.getMissions(any(), any(), eq(0), eq(20)))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/hr/workers/me/missions")
                        .with(user(workerUserDetails())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("티어 달성 미션 조회 — 200 OK")
    void getUpgradeMissions_success() throws Exception {
        given(missionQueryService.getUpgradeMissions(any()))
                .willReturn(List.of(
                        sampleMission(1L, "IN_PROGRESS"),
                        sampleMission(2L, "COMPLETED")));

        mockMvc.perform(get("/api/v1/hr/workers/me/missions/upgrade")
                        .with(user(workerUserDetails())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("티어 달성 미션 조회 — 미션 없으면 빈 배열 반환")
    void getUpgradeMissions_empty() throws Exception {
        given(missionQueryService.getUpgradeMissions(any())).willReturn(List.of());

        mockMvc.perform(get("/api/v1/hr/workers/me/missions/upgrade")
                        .with(user(workerUserDetails())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("티어 성장 히스토리 조회 — 입사 티어 포함")
    void getTierHistory_success() throws Exception {
        given(promotionQueryService.getWorkerTierHistory(any()))
                .willReturn(List.of(new WorkerTierHistoryItem(
                        "INITIAL", null, "C", null, null, "입사 티어",
                        LocalDate.of(2024, 1, 1), null)));

        mockMvc.perform(get("/api/v1/hr/workers/me/tier-history")
                        .with(user(workerUserDetails())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].eventType").value("INITIAL"))
                .andExpect(jsonPath("$.data[0].toTier").value("C"));
    }
}
