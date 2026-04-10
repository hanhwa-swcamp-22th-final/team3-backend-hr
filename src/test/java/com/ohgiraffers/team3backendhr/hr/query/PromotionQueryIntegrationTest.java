package com.ohgiraffers.team3backendhr.hr.query;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
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
class PromotionQueryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private IdGenerator idGenerator;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockitoBean  private AdminClient adminClient;

    private static final Long HRM_ID      = 999L;
    private static final Long EMPLOYEE_ID = 100L;
    /* tier_config_id — 테스트 DB에 실제 존재해야 합니다 */
    private static final Long CUR_TIER_ID = 1L;
    private static final Long TGT_TIER_ID = 2L;

    private Long savedId;

    private UsernamePasswordAuthenticationToken hrmAuth() {
        EmployeeUserDetails u = new EmployeeUserDetails(HRM_ID, "HRM001", "pw",
                List.of(new SimpleGrantedAuthority("HRM")));
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        savedId = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO promotion_history(tier_promotion_id, employee_id, reviewer_id, current_tier_config_id, target_tier_config_id, tier_accumulated_point, tier_promo_status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'UNDER_REVIEW', NOW(), NOW())",
                savedId, EMPLOYEE_ID, HRM_ID, CUR_TIER_ID, TGT_TIER_ID, 80);
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_name, employee_email, employee_role, employee_status, mfa_enabled, login_fail_count, is_locked) " +
                "VALUES (?, 1, '홍길동', 'hong@test.com', 'WORKER', 'ACTIVE', 0, 0, 0) " +
                "ON DUPLICATE KEY UPDATE employee_name = '홍길동'",
                EMPLOYEE_ID);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Test
    @DisplayName("승급 요약 조회 — 전체 수·확정 수 반환")
    void getSummary_success() throws Exception {
        mockMvc.perform(get("/api/v1/hr/promotions/summary")
                        .with(authentication(hrmAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCandidates").isNumber());
    }

    @Test
    @DisplayName("후보 목록 조회 — 페이징 결과 반환")
    void getCandidates_success() throws Exception {
        mockMvc.perform(get("/api/v1/hr/promotions/candidates")
                        .with(authentication(hrmAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("후보 상세 조회 — 저장된 데이터 반환")
    void getCandidateDetail_success() throws Exception {
        mockMvc.perform(get("/api/v1/hr/promotions/" + savedId)
                        .with(authentication(hrmAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tierPromotionId").value(savedId));
    }

    @Test
    @DisplayName("존재하지 않는 후보 상세 조회 — 400 반환")
    void getCandidateDetail_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/hr/promotions/99999999")
                        .with(authentication(hrmAuth())))
                .andExpect(status().isBadRequest());
    }
}
