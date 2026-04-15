package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealSummaryResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AppealQueryMapperTest {

    @Autowired
    private AppealQueryMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private static final Long WORKER_ID = 900_001L;
    private static final Long HRM_ID = 900_002L;
    private static final Long DEPT_ID = 800_001L;
    private static final Long PERIOD_ID = 700_001L;
    private static final Long EVAL_ID = 600_001L;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_name, employee_role, employee_status, employee_code, employee_password) VALUES (?,?,?,'WORKER','ACTIVE','W9001','pw')",
                WORKER_ID, DEPT_ID, "worker-user");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_name, employee_role, employee_status, employee_code, employee_password) VALUES (?,?,?,'HRM','ACTIVE','H9001','pw')",
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
        jdbcTemplate.execute("DELETE FROM score_modification_log WHERE score_evaluatee_id = " + WORKER_ID);
        jdbcTemplate.execute("DELETE FROM evaluation_appeal WHERE appeal_employee_id = " + WORKER_ID);
        jdbcTemplate.execute("DELETE FROM attachment_file_group WHERE reference_type = 'APPEAL'");
        jdbcTemplate.execute("DELETE FROM qualitative_evaluation WHERE qualitative_evaluation_id = " + EVAL_ID);
        jdbcTemplate.execute("DELETE FROM evaluation_period WHERE eval_period_id = " + PERIOD_ID);
        jdbcTemplate.execute("DELETE FROM employee WHERE employee_id IN (" + WORKER_ID + "," + HRM_ID + ")");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private long insertFileGroup() {
        long id = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO attachment_file_group(file_group_id, reference_id, reference_type) VALUES (?,?,'APPEAL')",
                id, EVAL_ID);
        return id;
    }

    private long insertAppeal(String status, String reviewResult) {
        long fileGroupId = insertFileGroup();
        long id = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO evaluation_appeal(appeal_id, appeal_employee_id, evaluation_period_id, appeal_type, title, content, status, review_result, anonymized_comparison, filed_at, file_group_id) VALUES (?,?,?,'SCORE_ERRORS','점수 오류 이의신청','20자 이상의 내용입니다. 재검토 요청드립니다.',?,?,?,?,?)",
                id, WORKER_ID, PERIOD_ID, status, reviewResult, EVAL_ID, LocalDateTime.now(), fileGroupId);
        return id;
    }

    @Test
    @DisplayName("findAppealById returns detail for existing appeal")
    void findAppealById_success() {
        long appealId = insertAppeal("RECEIVING", null);

        Optional<AppealDetailResponse> result = mapper.findAppealById(appealId);

        assertThat(result).isPresent();
        assertThat(result.get().getAppealId()).isEqualTo(appealId);
        assertThat(result.get().getContent()).isNotBlank();
        assertThat(result.get().getEmployeeName()).isEqualTo("worker-user");
    }

    @Test
    @DisplayName("findAppealById returns empty optional for missing appeal")
    void findAppealById_notFound() {
        Optional<AppealDetailResponse> result = mapper.findAppealById(999_999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAppeals without filter returns inserted appeals")
    void findAppeals_noFilter() {
        insertAppeal("RECEIVING", null);

        List<AppealSummaryResponse> result = mapper.findAppeals(null, 10, 0);

        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
        assertThat(result).anyMatch(r -> r.getAppealEmployeeId().equals(WORKER_ID));
    }

    @Test
    @DisplayName("findAppeals applies receiving status filter")
    void findAppeals_withStatusFilter() {
        insertAppeal("RECEIVING", null);
        insertAppeal("COMPLETED", "DISMISS");

        List<AppealSummaryResponse> result = mapper.findAppeals("RECEIVING", 10, 0);

        assertThat(result).allMatch(r -> r.getStatus().equals("RECEIVING"));
    }

    @Test
    @DisplayName("findAppeals applies completed status filter")
    void findAppeals_completedFilter() {
        // given
        jdbcTemplate.update(
                "UPDATE qualitative_evaluation SET status = 'SUBMITTED' WHERE qualitative_evaluation_id = ?",
                EVAL_ID);
        insertAppeal("COMPLETED", "ACKNOWLEDGE");

        List<AppealSummaryResponse> result = mapper.findAppeals("COMPLETED", 10, 0);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.getStatus().equals("COMPLETED"));
    }

    @Test
    @DisplayName("상태 필터 COMPLETED — 기각 완료 건도 조회된다")
    void findAppeals_completedFilter_includesDismissed() {
        // given
        insertAppeal("COMPLETED", "DISMISS");

        // when
        List<AppealSummaryResponse> result = mapper.findAppeals("COMPLETED", 10, 0);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(r ->
                "COMPLETED".equals(r.getStatus()) && "DISMISS".equals(r.getReviewResult()));
    }

    /* ── countAppeals ──────────────────────────────────────────────── */


    @Test
    @DisplayName("countAppeals counts filtered rows")
    void countAppeals_withStatusFilter() {
        insertAppeal("RECEIVING", null);
        insertAppeal("COMPLETED", "DISMISS");

        long count = mapper.countAppeals("RECEIVING");

        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("findMyAppeals returns only my appeals")
    void findMyAppeals_onlyMine() {
        insertAppeal("RECEIVING", null);

        List<AppealSummaryResponse> result = mapper.findMyAppeals(WORKER_ID);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.getAppealEmployeeId().equals(WORKER_ID));
    }

    @Test
    @DisplayName("findMyAppeals returns empty list when nothing exists")
    void findMyAppeals_empty() {
        List<AppealSummaryResponse> result = mapper.findMyAppeals(999_999L);

        assertThat(result).isEmpty();
    }
}
