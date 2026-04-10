package com.ohgiraffers.team3backendhr.hr.command.application.controller.teamleader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.QualitativeEvaluationCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.InputMethod;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualEvalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamLeaderEvaluationCommandController.class)
class TeamLeaderEvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QualitativeEvaluationCommandService service;

    private EmployeeUserDetails tlUser() {
        return new EmployeeUserDetails(200L, "EMP001", "password",
                List.of(new SimpleGrantedAuthority("TL")));
    }

    @Test
    @DisplayName("1차 평가 임시저장 성공 — 200 OK")
    void saveDraft_success() throws Exception {
        QualitativeEvaluationUpdateRequest request = new QualitativeEvaluationUpdateRequest(
                QualEvalStatus.DRAFT, 5L, "{\"TECHNICAL_COMPETENCE\": 80}",
                "이번 분기 설비 대응 역량이 크게 향상되었습니다.", InputMethod.TEXT);

        mockMvc.perform(patch("/api/v1/hr/team-leader/evaluations/101")
                        .with(csrf())
                        .with(user(tlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).updateForTL(eq(200L), eq(101L), any(QualitativeEvaluationUpdateRequest.class));
    }

    @Test
    @DisplayName("1차 평가 임시저장 — inputMethod 누락 시 400")
    void saveDraft_fail_missingInputMethod() throws Exception {
        QualitativeEvaluationUpdateRequest request = new QualitativeEvaluationUpdateRequest(
                QualEvalStatus.DRAFT, 5L, null, null, null);

        mockMvc.perform(patch("/api/v1/hr/team-leader/evaluations/101")
                        .with(csrf())
                        .with(user(tlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("1차 평가 임시저장 — 이미 제출된 평가면 400")
    void saveDraft_fail_alreadySubmitted() throws Exception {
        QualitativeEvaluationUpdateRequest request = new QualitativeEvaluationUpdateRequest(
                QualEvalStatus.DRAFT, 5L, null, null, InputMethod.TEXT);

        doThrow(new BusinessException(ErrorCode.EVALUATION_ALREADY_SUBMITTED))
                .when(service).updateForTL(any(), any(), any());

        mockMvc.perform(patch("/api/v1/hr/team-leader/evaluations/101")
                        .with(csrf())
                        .with(user(tlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("1차 평가 제출 성공 — 200 OK")
    void submit_success() throws Exception {
        QualitativeEvaluationUpdateRequest request = new QualitativeEvaluationUpdateRequest(
                QualEvalStatus.SUBMITTED, 5L, "{\"TECHNICAL_COMPETENCE\": 90}",
                "이번 분기 설비 대응 역량이 크게 향상되었으며 안전 수칙도 철저히 준수하였습니다.",
                InputMethod.TEXT);

        mockMvc.perform(patch("/api/v1/hr/team-leader/evaluations/101")
                        .with(csrf())
                        .with(user(tlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).updateForTL(eq(200L), eq(101L), any(QualitativeEvaluationUpdateRequest.class));
    }

    @Test
    @DisplayName("1차 평가 제출 — 코멘트 20자 미만이면 400")
    void submit_fail_commentTooShort() throws Exception {
        doThrow(new BusinessException(ErrorCode.INVALID_COMMENT_LENGTH))
                .when(service).updateForTL(any(), any(), any());

        QualitativeEvaluationUpdateRequest request = new QualitativeEvaluationUpdateRequest(
                QualEvalStatus.SUBMITTED, 5L, null, "짧은 코멘트", InputMethod.TEXT);

        mockMvc.perform(patch("/api/v1/hr/team-leader/evaluations/101")
                        .with(csrf())
                        .with(user(tlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
