package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationConfirmRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.QualitativeEvaluationCommandService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HrmEvaluationCommandController.class)
class HrmEvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QualitativeEvaluationCommandService service;

    private EmployeeUserDetails hrmUser() {
        return new EmployeeUserDetails(400L, "HRM001", "password",
                List.of(new SimpleGrantedAuthority("HRM")));
    }

    @Test
    @DisplayName("3차 최종 확정 성공 — 200 OK")
    void confirmFinal_success() throws Exception {
        // given
        QualitativeEvaluationConfirmRequest request = new QualitativeEvaluationConfirmRequest(
                5L, "3차 최종 확정 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        // when & then
        mockMvc.perform(post("/api/v1/hr/hr-manager/evaluations/101/confirm")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).confirmFinal(eq(400L), eq(101L), any(QualitativeEvaluationConfirmRequest.class));
    }

    @Test
    @DisplayName("3차 최종 확정 — evalComment 누락 시 400")
    void confirmFinal_fail_missingComment() throws Exception {
        // given: evalComment null
        QualitativeEvaluationConfirmRequest request = new QualitativeEvaluationConfirmRequest(
                5L, null, InputMethod.TEXT);

        // when & then
        mockMvc.perform(post("/api/v1/hr/hr-manager/evaluations/101/confirm")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("3차 최종 확정 — 코멘트 20자 미만이면 400")
    void confirmFinal_fail_commentTooShort() throws Exception {
        // given
        QualitativeEvaluationConfirmRequest request = new QualitativeEvaluationConfirmRequest(
                5L, "짧은코멘트", InputMethod.TEXT);

        // when & then
        mockMvc.perform(post("/api/v1/hr/hr-manager/evaluations/101/confirm")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("3차 최종 확정 — 2차 미제출이면 400")
    void confirmFinal_fail_level2NotSubmitted() throws Exception {
        // given
        QualitativeEvaluationConfirmRequest request = new QualitativeEvaluationConfirmRequest(
                5L, "3차 최종 확정 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        doThrow(new IllegalStateException("2차 평가가 제출되지 않아 최종 확정을 진행할 수 없습니다."))
                .when(service).confirmFinal(any(), any(), any());

        // when & then
        mockMvc.perform(post("/api/v1/hr/hr-manager/evaluations/101/confirm")
                        .with(csrf())
                        .with(user(hrmUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
