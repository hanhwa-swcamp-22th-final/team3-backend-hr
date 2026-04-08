package com.ohgiraffers.team3backendhr.hr.command.application.controller.teamleader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.QualitativeEvaluationCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.InputMethod;
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

/**
 * [Controller 계층 테스트] TeamLeaderEvaluationCommandController
 *
 * - 전략: @WebMvcTest + MockMvc 로 웹 계층만 분리하여 가볍게 검증
 *   → Service 는 @MockitoBean 으로 대체하고, HTTP 통신 자체(URL 매핑, 상태코드, JSON 응답)에 집중
 * - 비즈니스 로직의 정합성은 QualitativeEvaluationServiceTest 에서 집중 검증
 */
@WebMvcTest(TeamLeaderEvaluationCommandController.class)
class TeamLeaderEvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Service 는 웹 계층 테스트와 무관하므로 Mock 으로 대체
    @MockitoBean
    private QualitativeEvaluationCommandService service;

    /**
     * TL 권한을 가진 가짜 인증 사용자 생성 헬퍼
     * - employeeId=200, empCode="EMP001"
     * - SimpleGrantedAuthority: String 권한명을 Spring Security 권한 객체로 변환
     */
    private EmployeeUserDetails tlUser() {
        return new EmployeeUserDetails(200L, "EMP001", "password",
                List.of(new SimpleGrantedAuthority("TL")));
    }

    /* ── saveDraft (임시저장) ─────────────────────────────────────────────── */

    /**
     * [Happy Path] 필수 필드가 모두 유효하면 Service 가 호출되고 200 OK 를 반환한다.
     * - verify: 컨트롤러가 JWT 에서 추출한 evaluatorId(200L)와 경로변수 employeeId(101L)를
     *   올바르게 Service 로 전달하는지 확인
     */
    @Test
    @DisplayName("1차 평가 임시저장 성공 — 200 OK")
    void saveDraft_success() throws Exception {
        // given: 유효한 임시저장 요청 (evalPeriodId, evalItems, comment, inputMethod 포함)
        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, "{\"TECHNICAL_COMPETENCE\": 80}", "이번 분기 설비 대응 역량이 크게 향상되었습니다.", InputMethod.TEXT);

        // when & then: POST /api/v1/hr/team-leader/evaluations/101/draft 요청 → 200 OK
        mockMvc.perform(post("/api/v1/hr/team-leader/evaluations/101/draft")
                        .with(csrf())
                        .with(user(tlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then: 컨트롤러가 인증 사용자 ID(200L)와 경로변수(101L)를 Service 에 정확히 위임했는지 검증
        verify(service).saveDraft(eq(200L), eq(101L), any(QualitativeEvaluationDraftRequest.class));
    }

    /**
     * [입력 검증] inputMethod 는 @NotNull 필드 — null 이면 Bean Validation 에서 400 을 반환해야 한다.
     * - Service 호출 없이 웹 계층 검증만으로 차단되는지 확인
     */
    @Test
    @DisplayName("1차 평가 임시저장 — inputMethod 누락 시 400")
    void saveDraft_fail_missingInputMethod() throws Exception {
        // given: inputMethod 가 null 인 요청
        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, null, null, null);

        // when & then: Bean Validation 실패 → 400 Bad Request
        mockMvc.perform(post("/api/v1/hr/team-leader/evaluations/101/draft")
                        .with(csrf())
                        .with(user(tlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * [비즈니스 예외] 이미 SUBMITTED 상태인 평가를 임시저장하면 Service 가 IllegalStateException 을 던지고
     * GlobalExceptionHandler 가 이를 400 으로 변환해야 한다.
     */
    @Test
    @DisplayName("1차 평가 임시저장 — 이미 제출된 평가면 400")
    void saveDraft_fail_alreadySubmitted() throws Exception {
        // given: 유효한 요청이지만 Service 가 예외를 던지도록 설정
        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, null, null, InputMethod.TEXT);

        doThrow(new IllegalStateException("이미 제출된 평가는 수정할 수 없습니다."))
                .when(service).saveDraft(any(), any(), any());

        // when & then: Service 예외 → GlobalExceptionHandler → 400 Bad Request
        mockMvc.perform(post("/api/v1/hr/team-leader/evaluations/101/draft")
                        .with(csrf())
                        .with(user(tlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /* ── submit (최종 제출) ───────────────────────────────────────────────── */

    /**
     * [Happy Path] 모든 필드가 유효하면 Service 가 호출되고 200 OK 를 반환한다.
     * - 임시저장과 달리 score 필드가 추가로 필요 (0~100 범위)
     */
    @Test
    @DisplayName("1차 평가 제출 성공 — 200 OK")
    void submit_success() throws Exception {
        // given
        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, "{\"TECHNICAL_COMPETENCE\": 90}",
                "이번 분기 설비 대응 역량이 크게 향상되었으며 안전 수칙도 철저히 준수하였습니다.",
                InputMethod.TEXT);

        // when & then
        mockMvc.perform(post("/api/v1/hr/team-leader/evaluations/101/submit")
                        .with(csrf())
                        .with(user(tlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).submit(eq(200L), eq(101L), any(QualitativeEvaluationSubmitRequest.class));
    }

    @Test
    @DisplayName("1차 평가 제출 — 코멘트 20자 미만이면 400")
    void submit_fail_commentTooShort() throws Exception {
        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "짧은 코멘트", InputMethod.TEXT);

        mockMvc.perform(post("/api/v1/hr/team-leader/evaluations/101/submit")
                        .with(csrf())
                        .with(user(tlUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
