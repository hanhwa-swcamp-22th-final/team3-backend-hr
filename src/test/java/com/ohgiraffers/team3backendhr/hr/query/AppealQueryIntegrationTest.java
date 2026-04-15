package com.ohgiraffers.team3backendhr.hr.query;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class AppealQueryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private IdGenerator idGenerator;
    @MockitoBean private AdminClient adminClient;

    private static final Long WORKER_ID = 910_001L;
    private static final Long HRM_ID = 910_002L;
    private static final Long DEPT_ID = 810_001L;
    private static final Long PERIOD_ID = 710_001L;
    private static final Long EVAL_ID = 610_001L;

    private UsernamePasswordAuthenticationToken workerAuth() {
        EmployeeUserDetails u = new EmployeeUserDetails(WORKER_ID, "W9101", "pw",
                List.of(new SimpleGrantedAuthority("WORKER")));
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    private UsernamePasswordAuthenticationToken hrmAuth() {
        EmployeeUserDetails u = new EmployeeUserDetails(HRM_ID, "H9101", "pw",
                List.of(new SimpleGrantedAuthority("HRM")));
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_name, employee_role, employee_status, employee_code, employee_password) VALUES (?,?,?,'WORKER','ACTIVE','W9101','pw')",
                WORKER_ID, DEPT_ID, "worker-user");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_name, employee_role, employee_status, employee_code, employee_password) VALUES (?,?,?,'HRM','ACTIVE','H9101','pw')",
                HRM_ID, DEPT_ID, "hrm-user");
        jdbcTemplate.update(
                "INSERT INTO evaluation_period(eval_period_id, algorithm_version_id, eval_year, eval_sequence, start_date, end_date, status) VALUES (?,1,2026,1,?,?,'IN_PROGRESS')",
                PERIOD_ID, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
        jdbcTemplate.update(
                "INSERT INTO qualitative_evaluation(qualitative_evaluation_id, evaluatee_id, evaluation_period_id, evaluation_level, status, score) VALUES (?,?,?,3,'CONFIRMED',80.0)",
                EVAL_ID, WORKER_ID, PERIOD_ID);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private long insertAppeal(String status) {
        long fileGroupId = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO attachment_file_group(file_group_id, reference_id, reference_type) VALUES (?,?,'APPEAL')",
                fileGroupId, EVAL_ID);
        long id = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO evaluation_appeal(appeal_id, appeal_employee_id, evaluation_period_id, appeal_type, title, content, status, anonymized_comparison, filed_at, file_group_id) VALUES (?,?,?,'SCORE_ERRORS','점수 오류 이의신청','20자 이상의 내용입니다. 재검토 요청드립니다.',?,?,?,?)",
                id, WORKER_ID, PERIOD_ID, status, EVAL_ID, LocalDateTime.now(), fileGroupId);
        return id;
    }

    @Test
    @DisplayName("HRM appeal list query returns content")
    void getAppeals_success() throws Exception {
        insertAppeal("RECEIVING");

        mockMvc.perform(get("/api/v1/hr/appeals")
                        .with(authentication(hrmAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("appeal list applies status filter")
    void getAppeals_withStatusFilter() throws Exception {
        insertAppeal("RECEIVING");

        mockMvc.perform(get("/api/v1/hr/appeals?status=RECEIVING")
                        .with(authentication(hrmAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].status").value("RECEIVING"));
    }

    @Test
    @DisplayName("worker appeal list returns own appeals")
    void getMyAppeals_success() throws Exception {
        insertAppeal("RECEIVING");

        mockMvc.perform(get("/api/v1/hr/appeals/me")
                        .with(authentication(workerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
