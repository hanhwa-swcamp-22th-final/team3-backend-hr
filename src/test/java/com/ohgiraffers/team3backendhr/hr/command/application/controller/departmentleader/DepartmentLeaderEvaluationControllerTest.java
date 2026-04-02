package com.ohgiraffers.team3backendhr.hr.command.application.controller.departmentleader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.QualitativeEvaluationService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.InputMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentLeaderEvaluationController.class)
class DepartmentLeaderEvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QualitativeEvaluationService service;

    private EmployeeUserDetails dlUser() {
        return new EmployeeUserDetails(300L, "EMP002", "password",
                List.of(new SimpleGrantedAuthority("ROLE_DL")));
    }

    /* ── saveDraft ───────────────────────────────────────────────────────── */

    @Test
    @DisplayName("2차 평가 임시저장 성공 — 200 OK")
    void saveDraft_success() throws Exception {
        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, "{\"LEADERSHIP\": 85}", "2차 임시저장 코멘트입니다.", InputMethod.TEXT);

        mockMvc.perform(post("/api/v1/hr/department-leader/evaluations/101/draft")
                        .with(csrf())
                        .with(user(dlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).saveDraftForDL(eq(300L), eq(101L), any(QualitativeEvaluationDraftRequest.class));
    }

    @Test
    @DisplayName("2차 평가 임시저장 — inputMethod 누락 시 400")
    void saveDraft_fail_missingInputMethod() throws Exception {
        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, null, null, null);

        mockMvc.perform(post("/api/v1/hr/department-leader/evaluations/101/draft")
                        .with(csrf())
                        .with(user(dlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("2차 평가 임시저장 — 이미 제출된 평가면 400")
    void saveDraft_fail_alreadySubmitted() throws Exception {
        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, null, null, InputMethod.TEXT);

        doThrow(new IllegalStateException("이미 제출된 평가는 수정할 수 없습니다."))
                .when(service).saveDraftForDL(any(), any(), any());

        mockMvc.perform(post("/api/v1/hr/department-leader/evaluations/101/draft")
                        .with(csrf())
                        .with(user(dlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /* ── submit ──────────────────────────────────────────────────────────── */

    @Test
    @DisplayName("2차 평가 제출 성공 — 200 OK")
    void submit_success() throws Exception {
        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, "{\"LEADERSHIP\": 90}",
                "2차 평가 코멘트입니다. 리더십과 팀 관리 역량이 우수하여 높은 점수를 부여합니다.",
                InputMethod.TEXT, 88.0);

        mockMvc.perform(post("/api/v1/hr/department-leader/evaluations/101/submit")
                        .with(csrf())
                        .with(user(dlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).submitForDL(eq(300L), eq(101L), any(QualitativeEvaluationSubmitRequest.class));
    }

    @Test
    @DisplayName("2차 평가 제출 — 코멘트 20자 미만이면 400")
    void submit_fail_commentTooShort() throws Exception {
        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "짧은 코멘트", InputMethod.TEXT, 85.0);

        mockMvc.perform(post("/api/v1/hr/department-leader/evaluations/101/submit")
                        .with(csrf())
                        .with(user(dlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("2차 평가 제출 — score 100 초과이면 400")
    void submit_fail_scoreOutOfRange() throws Exception {
        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "2차 평가 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT, 105.0);

        mockMvc.perform(post("/api/v1/hr/department-leader/evaluations/101/submit")
                        .with(csrf())
                        .with(user(dlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
