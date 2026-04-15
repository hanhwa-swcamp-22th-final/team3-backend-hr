package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.EvaluationPeriodCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvalPeriodStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EvaluationPeriodCommandController.class)
class EvaluationPeriodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EvaluationPeriodCommandService service;

    @Test
    @DisplayName("평가 기간을 생성한다")
    @WithMockUser(authorities = "HRM")
    void create_success() throws Exception {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );

        mockMvc.perform(post("/api/v1/hr/evaluation-periods")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).create(any(EvaluationPeriodCreateRequest.class));
    }

    @Test
    @DisplayName("이미 진행 중인 평가 기간이 있으면 400을 반환한다")
    @WithMockUser(authorities = "HRM")
    void create_fail_alreadyInProgress() throws Exception {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );
        doThrow(new BusinessException(ErrorCode.EVAL_PERIOD_ALREADY_IN_PROGRESS))
                .when(service).create(any());

        mockMvc.perform(post("/api/v1/hr/evaluation-periods")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("평가 기간을 마감한다")
    @WithMockUser(authorities = "HRM")
    void close_success() throws Exception {
        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                null, null, null, EvalPeriodStatus.CLOSING);

        mockMvc.perform(patch("/api/v1/hr/evaluation-periods/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).close(1L);
    }

    @Test
    @DisplayName("평가 기간을 확정한다")
    @WithMockUser(authorities = "HRM")
    void confirm_success() throws Exception {
        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                null, null, null, EvalPeriodStatus.CONFIRMED);

        mockMvc.perform(patch("/api/v1/hr/evaluation-periods/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).confirm(1L);
    }

    @Test
    @DisplayName("평가 기간을 수정한다")
    @WithMockUser(authorities = "HRM")
    void update_success() throws Exception {
        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 4, 30),
                2L,
                null
        );

        mockMvc.perform(patch("/api/v1/hr/evaluation-periods/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).update(eq(1L), any(EvaluationPeriodUpdateRequest.class));
    }
}
