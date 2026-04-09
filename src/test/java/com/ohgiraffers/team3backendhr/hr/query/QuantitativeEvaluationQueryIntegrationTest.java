package com.ohgiraffers.team3backendhr.hr.query;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuantitativeEvaluationQueryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private IdGenerator idGenerator;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockitoBean  private AdminClient adminClient;

    private static final Long WORKER_ID   = 400L;
    private static final Long DEPT_ID     = 10L;
    private static final Long EQUIPMENT_ID = 1L;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_code, employee_name, employee_role, employee_status, employee_password, mfa_enabled, login_fail_count, is_locked) VALUES (?,?,?,?,'WORKER','ACTIVE','pw',false,0,false)",
                WORKER_ID, DEPT_ID, "W002", "워커2");
    }

    @AfterEach
    void tearDown() { jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1"); }

    private long insertPeriod(int year, int seq, String status) {
        long id = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO evaluation_period(eval_period_id, algorithm_version_id, eval_year, eval_sequence, eval_type, start_date, end_date, status) VALUES (?,1,?,?,'QUANTITATIVE',?,?,?)",
                id, year, seq, year + "-01-01", year + "-03-31", status);
        return id;
    }

    private long insertQuantEval(long periodId, String status) {
        long id = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO quantitative_evaluation(quantitative_evaluation_id, employee_id, eval_period_id, equipment_id, uph_score, yield_score, lead_time_score, actual_error, s_quant, t_score, material_shielding, status) VALUES (?,?,?,?,90.0,85.0,88.0,0.02,87.5,91.0,false,?)",
                id, WORKER_ID, periodId, EQUIPMENT_ID, status);
        return id;
    }

    @Test
    @WithMockUser(authorities = "HRM")
    @DisplayName("정량 평가 목록을 조회하면 저장된 데이터를 반환한다")
    void getList_success() throws Exception {
        long periodId = insertPeriod(2026, 1, "IN_PROGRESS");
        insertQuantEval(periodId, "TEMPORARY");

        mockMvc.perform(get("/api/v1/hr/evaluations/quantitative"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].employeeId").value(WORKER_ID));
    }

    @Test
    @WithMockUser(authorities = "HRM")
    @DisplayName("periodId 필터로 특정 기간의 정량 평가만 조회한다")
    void getList_filterByPeriodId() throws Exception {
        long periodId1 = insertPeriod(2026, 1, "IN_PROGRESS");
        long periodId2 = insertPeriod(2025, 1, "CONFIRMED");
        insertQuantEval(periodId1, "TEMPORARY");
        insertQuantEval(periodId2, "CONFIRMED");

        mockMvc.perform(get("/api/v1/hr/evaluations/quantitative")
                        .param("periodId", String.valueOf(periodId1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.content[0].evalYear").value(2026));
    }

    @Test
    @WithMockUser(authorities = "HRM")
    @DisplayName("status 필터로 CONFIRMED 상태만 조회한다")
    void getList_filterByStatus() throws Exception {
        long periodId = insertPeriod(2026, 1, "IN_PROGRESS");
        insertQuantEval(periodId, "TEMPORARY");
        insertQuantEval(periodId, "CONFIRMED");

        mockMvc.perform(get("/api/v1/hr/evaluations/quantitative")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(authorities = "HRM")
    @DisplayName("정량 평가 상세 조회 시 전체 점수 항목을 반환한다")
    void getDetail_success() throws Exception {
        long periodId = insertPeriod(2026, 1, "IN_PROGRESS");
        long evalId = insertQuantEval(periodId, "TEMPORARY");

        mockMvc.perform(get("/api/v1/hr/evaluations/quantitative/" + evalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeId").value(WORKER_ID))
                .andExpect(jsonPath("$.data.equipmentId").value(EQUIPMENT_ID))
                .andExpect(jsonPath("$.data.uphScore").value(90.0));
    }

    @Test
    @WithMockUser(authorities = "HRM")
    @DisplayName("존재하지 않는 ID로 상세 조회 시 404를 반환한다")
    void getDetail_fail_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/hr/evaluations/quantitative/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "WORKER")
    @DisplayName("HRM 권한이 없으면 목록 조회 시 403을 반환한다")
    void getList_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/hr/evaluations/quantitative"))
                .andExpect(status().isForbidden());
    }
}
