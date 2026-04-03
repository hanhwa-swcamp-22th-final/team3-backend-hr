package com.ohgiraffers.team3backendhr.hr.query;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
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
class DlEvaluationQueryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private IdGenerator idGenerator;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockitoBean  private AdminClient adminClient;

    private static final Long DL_ID     = 300L;
    private static final Long WORKER_ID = 301L;
    private static final Long DEPT_ID   = 20L;

    private UsernamePasswordAuthenticationToken dlAuth() {
        EmployeeUserDetails userDetails = new EmployeeUserDetails(
                DL_ID, "DL001", "password",
                List.of(new SimpleGrantedAuthority("DL")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_code, employee_name, employee_role, employee_status, employee_password, mfa_enabled, login_fail_count, is_locked) VALUES (?,?,?,?,'DL','ACTIVE','pw',false,0,false)",
                DL_ID, DEPT_ID, "DL001", "부서장");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_code, employee_name, employee_role, employee_status, employee_password, mfa_enabled, login_fail_count, is_locked) VALUES (?,?,?,?,'WORKER','ACTIVE','pw',false,0,false)",
                WORKER_ID, DEPT_ID, "W001", "워커");
    }

    @AfterEach
    void tearDown() { jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1"); }

    private long insertPeriod(String status) {
        long id = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO evaluation_period(eval_period_id, algorithm_version_id, eval_year, eval_sequence, eval_type, start_date, end_date, status) VALUES (?,1,2026,1,'QUALITATIVE','2026-01-01','2026-03-31',?)",
                id, status);
        return id;
    }

    private void insertEval(long evalId, long evaluateeId, long periodId, int level, String status, Double score) {
        jdbcTemplate.update(
                "INSERT INTO qualitative_evaluation(qualitative_evaluation_id, evaluatee_id, evaluation_period_id, evaluation_level, status, score) VALUES (?,?,?,?,?,?)",
                evalId, evaluateeId, periodId, level, status, score);
    }

    @Test
    @DisplayName("level 1이 SUBMITTED이면 DL 평가 대상 목록과 1차 점수를 반환한다")
    void getTargets_success() throws Exception {
        // given
        long periodId = insertPeriod("IN_PROGRESS");
        insertEval(idGenerator.generate(), WORKER_ID, periodId, 1, "SUBMITTED", 80.0);  // level 1 제출 완료
        insertEval(idGenerator.generate(), WORKER_ID, periodId, 2, "NO_INPUT",  null);  // level 2 대기 중

        // when & then
        mockMvc.perform(get("/api/v1/hr/department-leader/evaluations/targets")
                        .param("periodId", String.valueOf(periodId))
                        .with(authentication(dlAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targets").isArray())
                .andExpect(jsonPath("$.data.targets[0].evaluateeId").value(WORKER_ID))
                .andExpect(jsonPath("$.data.targets[0].firstStageScore").value(80.0));
    }

    @Test
    @DisplayName("level 1이 미제출이면 DL 평가 대상 목록이 비어있다")
    void getTargets_emptyWhenLevel1NotSubmitted() throws Exception {
        // given
        long periodId = insertPeriod("IN_PROGRESS");
        insertEval(idGenerator.generate(), WORKER_ID, periodId, 1, "DRAFT",    null);  // level 1 미제출
        insertEval(idGenerator.generate(), WORKER_ID, periodId, 2, "NO_INPUT", null);  // level 2 대기 중

        // when & then
        mockMvc.perform(get("/api/v1/hr/department-leader/evaluations/targets")
                        .param("periodId", String.valueOf(periodId))
                        .with(authentication(dlAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targets").isEmpty());
    }

    @Test
    @DisplayName("periodId 없고 IN_PROGRESS 기간도 없으면 400을 반환한다")
    void getTargets_fail_noInProgress() throws Exception {
        mockMvc.perform(get("/api/v1/hr/department-leader/evaluations/targets")
                        .with(authentication(dlAuth())))
                .andExpect(status().isBadRequest());
    }
}
