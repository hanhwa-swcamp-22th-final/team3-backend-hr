package com.ohgiraffers.team3backendhr.hr.query;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkerEvaluationQueryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private IdGenerator idGenerator;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockitoBean  private AdminClient adminClient;

    private static final Long WORKER_ID = 300L;
    private static final Long DEPT_ID   = 10L;
    private static final Long EQUIPMENT_ID = 1L;

    private UsernamePasswordAuthenticationToken workerAuth() {
        EmployeeUserDetails userDetails = new EmployeeUserDetails(
                WORKER_ID, "W001", "password",
                List.of(new SimpleGrantedAuthority("WORKER")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        // 기존 IN_PROGRESS 기간이 LIMIT 1 쿼리에 간섭하지 않도록 임시 CLOSING 처리 (트랜잭션 롤백으로 복원됨)
        jdbcTemplate.update("UPDATE evaluation_period SET status = 'CLOSING' WHERE status = 'IN_PROGRESS'");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_code, employee_name, employee_role, employee_status, employee_password, mfa_enabled, login_fail_count, is_locked) VALUES (?,?,?,?,'WORKER','ACTIVE','pw',false,0,false)",
                WORKER_ID, DEPT_ID, "W001", "워커");
    }

    @AfterEach
    void tearDown() { jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1"); }

    private long insertPeriod(String status) {
        long id = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO evaluation_period(eval_period_id, algorithm_version_id, eval_year, eval_sequence, start_date, end_date, status) VALUES (?,1,2026,1,'2026-01-01','2026-03-31',?)",
                id, status);
        return id;
    }

    private void insertQualEval(long evalId, long periodId, int level, String status) {
        jdbcTemplate.update(
                "INSERT INTO qualitative_evaluation(qualitative_evaluation_id, evaluatee_id, evaluation_period_id, evaluation_level, status) VALUES (?,?,?,?,?)",
                evalId, WORKER_ID, periodId, level, status);
    }

    private void insertQuantEval(long evalId, long periodId) {
        jdbcTemplate.update(
                "INSERT INTO quantitative_evaluation(quantitative_evaluation_id, employee_id, eval_period_id, equipment_id, uph_score, yield_score, lead_time_score, actual_error, s_quant, t_score, material_shielding, status) VALUES (?,?,?,?,90.0,85.0,88.0,0.02,87.5,91.0,false,'CONFIRMED')",
                evalId, WORKER_ID, periodId, EQUIPMENT_ID);
    }

    // HR-EVAL-007
    @Test
    @DisplayName("IN_PROGRESS 기간이 있으면 내 평가 상태를 반환한다")
    void getEvalStatus_success() throws Exception {
        long periodId = insertPeriod("IN_PROGRESS");
        insertQualEval(idGenerator.generate(), periodId, 3, "NO_INPUT");

        mockMvc.perform(get("/api/v1/hr/workers/me/evaluations/status")
                        .with(authentication(workerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.evalPeriodId").value(periodId))
                .andExpect(jsonPath("$.data.qualStatus").value("NO_INPUT"));
    }

    @Test
    @DisplayName("IN_PROGRESS 기간이 없으면 400을 반환한다")
    void getEvalStatus_fail_noInProgress() throws Exception {
        mockMvc.perform(get("/api/v1/hr/workers/me/evaluations/status")
                        .with(authentication(workerAuth())))
                .andExpect(status().isBadRequest());
    }

    // HR-EVAL-008
    @Test
    @DisplayName("periodId 없이 호출하면 IN_PROGRESS 기간의 정량 평가를 반환한다")
    void getQuantitative_autoResolve() throws Exception {
        long periodId = insertPeriod("IN_PROGRESS");
        insertQuantEval(idGenerator.generate(), periodId);

        mockMvc.perform(get("/api/v1/hr/workers/me/evaluations/quantitative")
                        .with(authentication(workerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tscore").value(91.0))
                .andExpect(jsonPath("$.data.evalPeriodId").value(periodId));
    }

    @Test
    @DisplayName("정량 평가 데이터가 없으면 400을 반환한다")
    void getQuantitative_fail_notFound() throws Exception {
        long periodId = insertPeriod("IN_PROGRESS");

        mockMvc.perform(get("/api/v1/hr/workers/me/evaluations/quantitative")
                        .param("periodId", String.valueOf(periodId))
                        .with(authentication(workerAuth())))
                .andExpect(status().isNotFound());
    }

    // HR-EVAL-009
    @Test
    @DisplayName("periodId를 명시하면 해당 기간의 정성 평가(level 3)를 반환한다")
    void getQualitative_withPeriodId() throws Exception {
        long periodId = insertPeriod("CONFIRMED");
        jdbcTemplate.update(
                "INSERT INTO qualitative_evaluation(qualitative_evaluation_id, evaluatee_id, evaluation_period_id, evaluation_level, grade, score, status) VALUES (?,?,?,3,'A',88.0,'CONFIRMED')",
                idGenerator.generate(), WORKER_ID, periodId);

        mockMvc.perform(get("/api/v1/hr/workers/me/evaluations/qualitative")
                        .param("periodId", String.valueOf(periodId))
                        .with(authentication(workerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grade").value("A"))
                .andExpect(jsonPath("$.data.score").value(88.0));
    }

    // HR-EVAL-010
    @Test
    @DisplayName("피드백 목록을 TL/DL/HRM 레벨 순으로 반환한다")
    void getFeedback_success() throws Exception {
        long periodId = insertPeriod("CONFIRMED");
        insertQualEval(idGenerator.generate(), periodId, 1, "SUBMITTED");
        insertQualEval(idGenerator.generate(), periodId, 2, "SUBMITTED");
        insertQualEval(idGenerator.generate(), periodId, 3, "CONFIRMED");

        mockMvc.perform(get("/api/v1/hr/workers/me/evaluations/feedback")
                        .param("periodId", String.valueOf(periodId))
                        .with(authentication(workerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedbackItems.length()").value(3));
    }

    // HR-EVAL-011
    @Test
    @DisplayName("평가 이력 목록을 페이징하여 반환한다")
    void getEvalHistory_success() throws Exception {
        long periodId = insertPeriod("CONFIRMED");
        jdbcTemplate.update(
                "INSERT INTO qualitative_evaluation(qualitative_evaluation_id, evaluatee_id, evaluation_period_id, evaluation_level, grade, score, status, confirmed_at) VALUES (?,?,?,3,'B',75.0,'CONFIRMED', NOW())",
                idGenerator.generate(), WORKER_ID, periodId);

        mockMvc.perform(get("/api/v1/hr/workers/me/evaluations/history")
                        .with(authentication(workerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].grade").value("B"))
                .andExpect(jsonPath("$.data.totalCount").value(1));
    }
}
