package com.ohgiraffers.team3backendhr.hr.query;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import java.util.List;
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
    @MockitoBean private AdminClient adminClient;

    private static final Long DL_ID = 300L;
    private static final Long WORKER_ID = 301L;
    private static final Long DL_DEPT_ID = 920L;
    private static final Long WORKER_DEPT_ID = 921L;

    private UsernamePasswordAuthenticationToken dlAuth() {
        EmployeeUserDetails userDetails = new EmployeeUserDetails(
                DL_ID, "DL001", "password",
                List.of(new SimpleGrantedAuthority("DL")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("DELETE FROM qualitative_evaluation WHERE evaluation_period_id IN (SELECT eval_period_id FROM evaluation_period WHERE status = 'IN_PROGRESS')");
        jdbcTemplate.execute("DELETE FROM evaluation_period WHERE status = 'IN_PROGRESS'");
        jdbcTemplate.update("DELETE FROM employee WHERE employee_id IN (?, ?)", DL_ID, WORKER_ID);
        jdbcTemplate.update("DELETE FROM department WHERE department_id IN (?, ?)", WORKER_DEPT_ID, DL_DEPT_ID);
        jdbcTemplate.update(
                "INSERT INTO department(department_id, parent_department_id, department_name, team_name, depth, is_deleted) VALUES (?,?,?,?,?,false)",
                DL_DEPT_ID, null, "dl-department", null, "1");
        jdbcTemplate.update(
                "INSERT INTO department(department_id, parent_department_id, department_name, team_name, depth, is_deleted) VALUES (?,?,?,?,?,false)",
                WORKER_DEPT_ID, DL_DEPT_ID, "worker-department", null, "2");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_code, employee_name, employee_role, employee_status, employee_password, mfa_enabled, login_fail_count, is_locked) VALUES (?,?,?,?,'DL','ACTIVE','pw',false,0,false)",
                DL_ID, DL_DEPT_ID, "DL001", "dept-leader");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_code, employee_name, employee_role, employee_status, employee_password, mfa_enabled, login_fail_count, is_locked) VALUES (?,?,?,?,'WORKER','ACTIVE','pw',false,0,false)",
                WORKER_ID, WORKER_DEPT_ID, "W001", "worker-user");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private long insertPeriod(String status) {
        long id = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO evaluation_period(eval_period_id, algorithm_version_id, eval_year, eval_sequence, start_date, end_date, status) VALUES (?,1,2026,1,'2026-01-01','2026-03-31',?)",
                id, status);
        return id;
    }

    private void insertEval(long evalId, long evaluateeId, long periodId, int level, String status, Double score) {
        jdbcTemplate.update(
                "INSERT INTO qualitative_evaluation(qualitative_evaluation_id, evaluatee_id, evaluation_period_id, evaluation_level, status, score) VALUES (?,?,?,?,?,?)",
                evalId, evaluateeId, periodId, level, status, score);
    }

    @Test
    @DisplayName("level 1 submitted returns DL targets with first-stage score")
    void getTargets_success() throws Exception {
        long periodId = insertPeriod("IN_PROGRESS");
        insertEval(idGenerator.generate(), WORKER_ID, periodId, 1, "SUBMITTED", 80.0);
        insertEval(idGenerator.generate(), WORKER_ID, periodId, 2, "NO_INPUT", null);

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
    @DisplayName("level 1 not submitted returns empty DL target list")
    void getTargets_emptyWhenLevel1NotSubmitted() throws Exception {
        long periodId = insertPeriod("IN_PROGRESS");
        insertEval(idGenerator.generate(), WORKER_ID, periodId, 1, "DRAFT", null);
        insertEval(idGenerator.generate(), WORKER_ID, periodId, 2, "NO_INPUT", null);

        mockMvc.perform(get("/api/v1/hr/department-leader/evaluations/targets")
                        .param("periodId", String.valueOf(periodId))
                        .with(authentication(dlAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targets").isEmpty());
    }

    @Test
    @DisplayName("without periodId and no in-progress period returns 400")
    void getTargets_fail_noInProgress() throws Exception {
        mockMvc.perform(get("/api/v1/hr/department-leader/evaluations/targets")
                        .with(authentication(dlAuth())))
                .andExpect(status().isBadRequest());
    }
}
