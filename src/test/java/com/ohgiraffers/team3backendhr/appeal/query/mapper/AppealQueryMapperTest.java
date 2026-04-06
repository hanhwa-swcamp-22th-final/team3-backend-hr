package com.ohgiraffers.team3backendhr.appeal.query.mapper;

import com.ohgiraffers.team3backendhr.appeal.query.dto.response.AppealSummaryResponse;
import com.ohgiraffers.team3backendhr.appeal.query.dto.response.ScoreModificationLogResponse;
import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AppealQueryMapperTest {

    @Autowired
    private AppealQueryMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private static final Long WORKER_ID  = 900_001L;
    private static final Long HRM_ID     = 900_002L;
    private static final Long DEPT_ID    = 800_001L;
    private static final Long PERIOD_ID  = 700_001L;
    private static final Long EVAL_ID    = 600_001L;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_name, employee_role, employee_status, employee_code, employee_password) VALUES (?,?,?,'WORKER','ACTIVE','W9001','pw')",
                WORKER_ID, DEPT_ID, "테스트워커");
        jdbcTemplate.update(
                "INSERT INTO employee(employee_id, department_id, employee_name, employee_role, employee_status, employee_code, employee_password) VALUES (?,?,?,'HRM','ACTIVE','H9001','pw')",
                HRM_ID, DEPT_ID, "테스트HRM");
        jdbcTemplate.update(
                "INSERT INTO evaluation_period(eval_period_id, algorithm_version_id, eval_year, eval_sequence, eval_type, start_date, end_date, status) VALUES (?,1,2026,1,'QUALITATIVE',?,?,'IN_PROGRESS')",
                PERIOD_ID, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
        jdbcTemplate.update(
                "INSERT INTO qualitative_evaluation(qualitative_evaluation_id, evaluatee_id, evaluation_period_id, evaluation_level, status, score) VALUES (?,?,?,3,'CONFIRMED',80.0)",
                EVAL_ID, WORKER_ID, PERIOD_ID);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM score_modification_log WHERE score_evaluatee_id = " + WORKER_ID);
        jdbcTemplate.execute("DELETE FROM evaluation_appeal WHERE appeal_employee_id = " + WORKER_ID);
        jdbcTemplate.execute("DELETE FROM attachment_file_group WHERE reference_id = " + EVAL_ID);
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
                "INSERT INTO evaluation_appeal(appeal_id, qualitative_evaluation_id, appeal_employee_id, appeal_type, title, content, status, review_result, anonymized_comparison, filed_at, file_group_id) VALUES (?,?,?,'SCORE_ERRORS','점수 오류 이의신청','20자 이상의 내용입니다. 재검토 요청드립니다.',?,?,0,?,?)",
                id, EVAL_ID, WORKER_ID, status, reviewResult, LocalDateTime.now(), fileGroupId);
        return id;
    }

    private long insertScoreModificationLog() {
        long id = idGenerator.generate();
        jdbcTemplate.update(
                "INSERT INTO score_modification_log(score_modification_log_id, score_evaluatee_id, score_modifier_id, score_original_score, score_modified_score, score_reason, score_is_deletable, score_modified_at) VALUES (?,?,?,80.0,90.0,'점수 오류 확인됨',0,?)",
                id, WORKER_ID, HRM_ID, LocalDateTime.now());
        return id;
    }

    /* ── findAppeals ───────────────────────────────────────────────── */

    @Test
    @DisplayName("상태 필터 없이 전체 조회 — 저장한 이의신청이 포함된다")
    void findAppeals_noFilter() {
        // given
        insertAppeal("RECEIVING", null);

        // when
        List<AppealSummaryResponse> result = mapper.findAppeals(null, 10, 0);

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
        assertThat(result).anyMatch(r -> r.getAppealEmployeeId().equals(WORKER_ID));
    }

    @Test
    @DisplayName("상태 필터 RECEIVING — 해당 상태 이의신청만 반환")
    void findAppeals_withStatusFilter() {
        // given
        insertAppeal("RECEIVING", null);
        insertAppeal("COMPLETED", "DISMISS");

        // when
        List<AppealSummaryResponse> result = mapper.findAppeals("RECEIVING", 10, 0);

        // then
        assertThat(result).allMatch(r -> r.getStatus().equals("RECEIVING"));
    }

    @Test
    @DisplayName("상태 필터 COMPLETED — COMPLETED 이의신청만 반환")
    void findAppeals_completedFilter() {
        // given
        insertAppeal("COMPLETED", "ACKNOWLEDGE");

        // when
        List<AppealSummaryResponse> result = mapper.findAppeals("COMPLETED", 10, 0);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.getStatus().equals("COMPLETED"));
    }

    /* ── countAppeals ──────────────────────────────────────────────── */

    @Test
    @DisplayName("상태 필터로 카운트 — 해당 상태 건수만 반환")
    void countAppeals_withStatusFilter() {
        // given
        insertAppeal("RECEIVING", null);
        insertAppeal("COMPLETED", "DISMISS");

        // when
        long count = mapper.countAppeals("RECEIVING");

        // then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    /* ── findMyAppeals ─────────────────────────────────────────────── */

    @Test
    @DisplayName("내 이의신청 조회 — 본인 이의신청만 반환")
    void findMyAppeals_onlyMine() {
        // given
        insertAppeal("RECEIVING", null);

        // when
        List<AppealSummaryResponse> result = mapper.findMyAppeals(WORKER_ID);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.getAppealEmployeeId().equals(WORKER_ID));
    }

    @Test
    @DisplayName("내 이의신청 조회 — 데이터 없으면 빈 목록 반환")
    void findMyAppeals_empty() {
        // given — 데이터 없음

        // when
        List<AppealSummaryResponse> result = mapper.findMyAppeals(999_999L);

        // then
        assertThat(result).isEmpty();
    }

    /* ── findScoreModificationLogs ─────────────────────────────────── */

    @Test
    @DisplayName("점수 수정 이력 조회 — 저장한 이력이 포함된다")
    void findScoreModificationLogs_success() {
        // given
        insertScoreModificationLog();

        // when
        List<ScoreModificationLogResponse> result = mapper.findScoreModificationLogs();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(r -> r.getScoreEvaluateeId().equals(WORKER_ID));
    }

    @Test
    @DisplayName("점수 수정 이력 조회 — 수정자 이름이 포함된다")
    void findScoreModificationLogs_includesModifierName() {
        // given
        insertScoreModificationLog();

        // when
        List<ScoreModificationLogResponse> result = mapper.findScoreModificationLogs();

        // then
        assertThat(result).anyMatch(r -> "테스트HRM".equals(r.getModifierName()));
    }
}
