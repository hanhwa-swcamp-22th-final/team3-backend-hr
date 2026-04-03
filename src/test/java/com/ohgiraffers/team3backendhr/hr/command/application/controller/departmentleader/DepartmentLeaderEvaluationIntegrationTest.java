package com.ohgiraffers.team3backendhr.hr.command.application.controller.departmentleader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
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
class DepartmentLeaderEvaluationIntegrationTest {

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

    private static final Long TL_ID = 100L;
    private static final Long DL_ID = 200L;
    private static final Long EVALUATEE_ID = 101L;
    private static final Long PERIOD_ID = 5L;

    private UsernamePasswordAuthenticationToken dlAuth() {
        EmployeeUserDetails userDetails = new EmployeeUserDetails(
                DL_ID, "DL001", "password",
                List.of(new SimpleGrantedAuthority("ROLE_DL")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // level 1 — TL이 이미 제출한 상태
        repository.save(QualitativeEvaluation.builder()
                .qualitativeEvaluationId(idGenerator.generate())
                .evaluateeId(EVALUATEE_ID)
                .evaluatorId(TL_ID)
                .evaluationPeriodId(PERIOD_ID)
                .evaluationLevel(1L)
                .evalComment("1차 평가 코멘트입니다. 충분히 길게 작성하였습니다.")
                .score(85.0)
                .grade(Grade.A)
                .inputMethod(InputMethod.TEXT)
                .status(QualEvalStatus.SUBMITTED)
                .build());

        // level 2 — DL이 평가할 레코드
        repository.save(QualitativeEvaluation.builder()
                .qualitativeEvaluationId(idGenerator.generate())
                .evaluateeId(EVALUATEE_ID)
                .evaluationPeriodId(PERIOD_ID)
                .evaluationLevel(2L)
                .build());
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Test
    @DisplayName("2차 평가 임시저장 시 상태가 DRAFT로 변경된다")
    void saveDraft_success() throws Exception {
        // given
        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                PERIOD_ID, "{\"성과\":90}", "2차 임시저장 코멘트입니다.", InputMethod.TEXT);

        // when
        mockMvc.perform(post("/api/v1/hr/department-leader/evaluations/" + EVALUATEE_ID + "/draft")
                        .with(csrf())
                        .with(authentication(dlAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        QualitativeEvaluation eval = repository
                .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(EVALUATEE_ID, PERIOD_ID, 2L)
                .orElseThrow();
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.DRAFT);
        assertThat(eval.getEvaluatorId()).isEqualTo(DL_ID);
    }

    @Test
    @DisplayName("2차 평가 제출 시 상태가 SUBMITTED로 변경되고 grade가 산출된다")
    void submit_success() throws Exception {
        // given
        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                PERIOD_ID, "{\"성과\":92}", "2차 제출 코멘트입니다. 충분히 길게 작성하였습니다.", InputMethod.TEXT, 92.0);

        // when
        mockMvc.perform(post("/api/v1/hr/department-leader/evaluations/" + EVALUATEE_ID + "/submit")
                        .with(csrf())
                        .with(authentication(dlAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        QualitativeEvaluation eval = repository
                .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(EVALUATEE_ID, PERIOD_ID, 2L)
                .orElseThrow();
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.SUBMITTED);
        assertThat(eval.getEvaluatorId()).isEqualTo(DL_ID);
        assertThat(eval.getScore()).isEqualTo(92.0);
        assertThat(eval.getGrade().name()).isEqualTo("S");
    }

    @Test
    @DisplayName("1차 평가가 제출되지 않으면 2차 임시저장 시 400을 반환한다")
    void saveDraft_fail_level1NotSubmitted() throws Exception {
        // given — level 1을 NO_INPUT 상태로 덮어씀
        QualitativeEvaluation level1 = repository
                .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(EVALUATEE_ID, PERIOD_ID, 1L)
                .orElseThrow();
        repository.save(QualitativeEvaluation.builder()
                .qualitativeEvaluationId(level1.getQualitativeEvaluationId())
                .evaluateeId(EVALUATEE_ID)
                .evaluationPeriodId(PERIOD_ID)
                .evaluationLevel(1L)
                .build()); // status = NO_INPUT

        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                PERIOD_ID, null, "코멘트", InputMethod.TEXT);

        // when & then
        mockMvc.perform(post("/api/v1/hr/department-leader/evaluations/" + EVALUATEE_ID + "/draft")
                        .with(csrf())
                        .with(authentication(dlAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
