package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationConfirmRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.InputMethod;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualitativeEvaluation;
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
class HrmEvaluationIntegrationTest {

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
    private static final Long HRM_ID = 400L;
    private static final Long EVALUATEE_ID = 101L;
    private static final Long PERIOD_ID = 5L;

    private UsernamePasswordAuthenticationToken hrmAuth() {
        EmployeeUserDetails userDetails = new EmployeeUserDetails(
                HRM_ID, "HRM001", "password",
                List.of(new SimpleGrantedAuthority("HRM")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // level 1 — TL 제출 완료
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

        // level 2 — DL 제출 완료
        repository.save(QualitativeEvaluation.builder()
                .qualitativeEvaluationId(idGenerator.generate())
                .evaluateeId(EVALUATEE_ID)
                .evaluatorId(DL_ID)
                .evaluationPeriodId(PERIOD_ID)
                .evaluationLevel(2L)
                .evalComment("2차 평가 코멘트입니다. 충분히 길게 작성하였습니다.")
                .score(90.0)
                .grade(Grade.S)
                .inputMethod(InputMethod.TEXT)
                .status(QualEvalStatus.SUBMITTED)
                .build());

        // level 3 — HRM이 확정할 레코드
        repository.save(QualitativeEvaluation.builder()
                .qualitativeEvaluationId(idGenerator.generate())
                .evaluateeId(EVALUATEE_ID)
                .evaluationPeriodId(PERIOD_ID)
                .evaluationLevel(3L)
                .build());
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Test
    @DisplayName("3차 최종 확정 성공 — 상태가 CONFIRMED로 변경된다")
    void confirmFinal_success() throws Exception {
        // given
        QualitativeEvaluationConfirmRequest request = new QualitativeEvaluationConfirmRequest(
                PERIOD_ID, "3차 최종 확정 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        // when
        mockMvc.perform(post("/api/v1/hr/hr-manager/evaluations/" + EVALUATEE_ID + "/confirm")
                        .with(csrf())
                        .with(authentication(hrmAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        QualitativeEvaluation eval = repository
                .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(EVALUATEE_ID, PERIOD_ID, 3L)
                .orElseThrow();
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.CONFIRMED);
        assertThat(eval.getEvaluatorId()).isEqualTo(HRM_ID);
    }

    @Test
    @DisplayName("2차 평가가 미제출이면 3차 확정 시 400을 반환한다")
    void confirmFinal_fail_level2NotSubmitted() throws Exception {
        // given — level 2를 NO_INPUT 상태로 덮어씀
        QualitativeEvaluation level2 = repository
                .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(EVALUATEE_ID, PERIOD_ID, 2L)
                .orElseThrow();
        repository.save(QualitativeEvaluation.builder()
                .qualitativeEvaluationId(level2.getQualitativeEvaluationId())
                .evaluateeId(EVALUATEE_ID)
                .evaluationPeriodId(PERIOD_ID)
                .evaluationLevel(2L)
                .build()); // status = NO_INPUT

        QualitativeEvaluationConfirmRequest request = new QualitativeEvaluationConfirmRequest(
                PERIOD_ID, "3차 최종 확정 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        // when & then
        mockMvc.perform(post("/api/v1/hr/hr-manager/evaluations/" + EVALUATEE_ID + "/confirm")
                        .with(csrf())
                        .with(authentication(hrmAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
