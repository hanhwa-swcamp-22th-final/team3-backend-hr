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
class TlEvaluationQueryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private IdGenerator idGenerator;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockitoBean  private AdminClient adminClient;

    private static final Long TL_ID     = 200L;
    private static final Long WORKER_ID = 201L;
    private static final Long DEPT_ID   = 10L;

    private UsernamePasswordAuthenticationToken tlAuth() {
        EmployeeUserDetails userDetails = new EmployeeUserDetails(
                TL_ID, "TL001", "password",
                List.of(new SimpleGrantedAuthority("TL")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_code, employee_name, employee_role, employee_status, employee_password, mfa_enabled, login_fail_count, is_locked) VALUES (?,?,?,?,'TL','ACTIVE','pw',false,0,false)",
                TL_ID, DEPT_ID, "TL001", "팀장");
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

    private void insertEval(long evalId, long evaluateeId, long periodId, int level, String status) {
        jdbcTemplate.update(
                "INSERT INTO qualitative_evaluation(qualitative_evaluation_id, evaluatee_id, evaluation_period_id, evaluation_level, status) VALUES (?,?,?,?,?)",
                evalId, evaluateeId, periodId, level, status);
    }

    @Test
    @DisplayName("periodId를 명시하면 해당 기간의 TL 평가 대상 목록을 반환한다")
    void getTargets_withPeriodId() throws Exception {
        // given
        long periodId = insertPeriod("IN_PROGRESS");
        insertEval(idGenerator.generate(), WORKER_ID, periodId, 1, "NO_INPUT");

        // when & then
        mockMvc.perform(get("/api/v1/hr/team-leader/evaluations/targets")
                        .param("periodId", String.valueOf(periodId))
                        .with(authentication(tlAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targets").isArray())
                .andExpect(jsonPath("$.data.targets[0].evaluateeId").value(WORKER_ID))
                .andExpect(jsonPath("$.data.targets[0].submitted").value(false));
    }

    @Test
    @DisplayName("periodId를 전달하지 않으면 IN_PROGRESS 기간으로 자동 resolve된다")
    void getTargets_withoutPeriodId_autoResolve() throws Exception {
        // given
        long periodId = insertPeriod("IN_PROGRESS");
        insertEval(idGenerator.generate(), WORKER_ID, periodId, 1, "NO_INPUT");

        // when & then
        mockMvc.perform(get("/api/v1/hr/team-leader/evaluations/targets")
                        .with(authentication(tlAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evalPeriodId").value(periodId));
    }

    @Test
    @DisplayName("periodId 없고 IN_PROGRESS 기간도 없으면 400을 반환한다")
    void getTargets_fail_noInProgress() throws Exception {
        mockMvc.perform(get("/api/v1/hr/team-leader/evaluations/targets")
                        .with(authentication(tlAuth())))
                .andExpect(status().isBadRequest());
    }
}
