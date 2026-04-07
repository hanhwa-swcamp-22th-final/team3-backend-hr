package com.ohgiraffers.team3backendhr.hr.command.application.controller.teamleader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.InputMethod;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TeamLeaderEvaluationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QualitativeEvaluationRepository repository;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Long EVALUATOR_ID = 100L;
    private static final Long EVALUATEE_ID = 101L;
    private static final Long PERIOD_ID = 5L;

    private UsernamePasswordAuthenticationToken tlAuth() {
        EmployeeUserDetails userDetails = new EmployeeUserDetails(
                EVALUATOR_ID, "TL001", "password",
                List.of(new SimpleGrantedAuthority("TL")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 평가 기간 생성 시 선생성되는 level 1 레코드 세팅
        repository.save(QualitativeEvaluation.builder()
                .qualitativeEvaluationId(idGenerator.generate())
                .evaluateeId(EVALUATEE_ID)
                .evaluationPeriodId(PERIOD_ID)
                .evaluationLevel(1L)
                .build());
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Test
    @DisplayName("1차 평가 임시저장 시 상태가 DRAFT로 변경된다")
    void saveDraft_success() throws Exception {
        // given
        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                PERIOD_ID, "{\"성과\":80}", "임시저장 코멘트입니다.", InputMethod.TEXT);

        // when
        mockMvc.perform(post("/api/v1/hr/team-leader/evaluations/" + EVALUATEE_ID + "/draft")
                        .with(csrf())
                        .with(authentication(tlAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        QualitativeEvaluation eval = repository
                .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(EVALUATEE_ID, PERIOD_ID, 1L)
                .orElseThrow();
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.DRAFT);
        assertThat(eval.getEvaluatorId()).isEqualTo(EVALUATOR_ID);
    }

    @Test
    @DisplayName("1차 평가 제출 시 상태가 SUBMITTED로 변경되고 score·grade는 null이다 — batch 분석 전")
    void submit_success() throws Exception {
        // given
        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                PERIOD_ID, "{\"성과\":85}", "제출 코멘트입니다. 충분히 길게 작성하였습니다.", InputMethod.TEXT);

        // when
        mockMvc.perform(post("/api/v1/hr/team-leader/evaluations/" + EVALUATEE_ID + "/submit")
                        .with(csrf())
                        .with(authentication(tlAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        QualitativeEvaluation eval = repository
                .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(EVALUATEE_ID, PERIOD_ID, 1L)
                .orElseThrow();
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.SUBMITTED);
        assertThat(eval.getEvaluatorId()).isEqualTo(EVALUATOR_ID);
        assertThat(eval.getScore()).isNull();
        assertThat(eval.getGrade()).isNull();
    }

    @Test
    @DisplayName("이미 제출된 평가에 임시저장 시 400을 반환한다")
    void saveDraft_fail_alreadySubmitted() throws Exception {
        // given — 먼저 제출
        QualitativeEvaluationSubmitRequest submitRequest = new QualitativeEvaluationSubmitRequest(
                PERIOD_ID, null, "제출 코멘트입니다. 충분히 길게 작성하였습니다.", InputMethod.TEXT);
        mockMvc.perform(post("/api/v1/hr/team-leader/evaluations/" + EVALUATEE_ID + "/submit")
                        .with(csrf())
                        .with(authentication(tlAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk());

        // when — 제출 후 임시저장 시도
        QualitativeEvaluationDraftRequest draftRequest = new QualitativeEvaluationDraftRequest(
                PERIOD_ID, null, "수정 시도", InputMethod.TEXT);
        mockMvc.perform(post("/api/v1/hr/team-leader/evaluations/" + EVALUATEE_ID + "/draft")
                        .with(csrf())
                        .with(authentication(tlAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draftRequest)))
                .andExpect(status().isBadRequest());
    }
}
