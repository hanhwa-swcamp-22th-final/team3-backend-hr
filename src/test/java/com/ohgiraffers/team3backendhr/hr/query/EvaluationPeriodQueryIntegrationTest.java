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
class EvaluationPeriodQueryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private IdGenerator idGenerator;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockitoBean  private AdminClient adminClient;

    @BeforeEach
    void setUp() { jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0"); }

    @AfterEach
    void tearDown() { jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1"); }

    private void insertPeriod(long id, int year, int seq, String status) {
        jdbcTemplate.update(
                "INSERT INTO evaluation_period(eval_period_id, algorithm_version_id, eval_year, eval_sequence, eval_type, start_date, end_date, status) VALUES (?,1,?,?,'QUALITATIVE',?,?,?)",
                id, year, seq,
                year + "-01-01",
                year + "-03-31",
                status);
    }

    @Test
    @WithMockUser(authorities = "HRM")
    @DisplayName("평가 기간 목록을 조회하면 저장된 전체 기간을 반환한다")
    void getList_success() throws Exception {
        // given
        insertPeriod(idGenerator.generate(), 2026, 1, "IN_PROGRESS");
        insertPeriod(idGenerator.generate(), 2025, 2, "CONFIRMED");

        // when & then
        mockMvc.perform(get("/api/v1/hr/evaluation-periods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @WithMockUser(authorities = "HRM")
    @DisplayName("연도 필터를 전달하면 해당 연도의 기간만 반환된다")
    void getList_filterByYear() throws Exception {
        // given
        insertPeriod(idGenerator.generate(), 2026, 1, "IN_PROGRESS");
        insertPeriod(idGenerator.generate(), 2025, 1, "CONFIRMED");

        // when & then
        mockMvc.perform(get("/api/v1/hr/evaluation-periods").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].evalYear").value(2026));
    }

    @Test
    @WithMockUser(authorities = "HRM")
    @DisplayName("상태 필터를 전달하면 해당 상태의 기간만 반환된다")
    void getList_filterByStatus() throws Exception {
        // given
        insertPeriod(idGenerator.generate(), 2026, 1, "IN_PROGRESS");
        insertPeriod(idGenerator.generate(), 2025, 1, "CONFIRMED");

        // when & then
        mockMvc.perform(get("/api/v1/hr/evaluation-periods").param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(authorities = "TL")
    @DisplayName("HRM 권한이 없으면 목록 조회 시 403을 반환한다")
    void getList_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/hr/evaluation-periods"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "TL")
    @DisplayName("IN_PROGRESS 기간이 있으면 마감일을 반환한다")
    void getDeadline_success() throws Exception {
        // given
        insertPeriod(idGenerator.generate(), 2026, 1, "IN_PROGRESS");

        // when & then
        mockMvc.perform(get("/api/v1/hr/evaluation-periods/deadline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.evalYear").value(2026))
                .andExpect(jsonPath("$.data.daysRemaining").exists())
                .andExpect(jsonPath("$.data.endDate").value("2026-03-31"));
    }

    @Test
    @WithMockUser(authorities = "TL")
    @DisplayName("IN_PROGRESS 기간이 없으면 마감일 조회 시 400을 반환한다")
    void getDeadline_fail_noInProgress() throws Exception {
        // given — CONFIRMED 기간만 존재
        insertPeriod(idGenerator.generate(), 2025, 1, "CONFIRMED");

        // when & then
        mockMvc.perform(get("/api/v1/hr/evaluation-periods/deadline"))
                .andExpect(status().isBadRequest());
    }
}
