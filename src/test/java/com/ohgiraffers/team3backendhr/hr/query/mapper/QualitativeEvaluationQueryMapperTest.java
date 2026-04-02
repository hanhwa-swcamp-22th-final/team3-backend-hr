package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.EvaluationPeriodDeadlineResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.EvaluationPeriodSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/disable-fk.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class QualitativeEvaluationQueryMapperTest {

    @Autowired
    private QualitativeEvaluationQueryMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private long insertPeriod(int year, int sequence, String status) {
        long id = idGenerator.generate();
        jdbcTemplate.update("""
                INSERT INTO evaluation_period
                  (eval_period_id, algorithm_version_id, eval_year, eval_sequence,
                   eval_type, start_date, end_date, status)
                VALUES (?, 1, ?, ?, 'QUALITATIVE', ?, ?, ?)
                """,
                id, year, sequence,
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 3, 31),
                status);
        return id;
    }

    /* ── findEvaluationPeriods ────────────────────────────────────────── */

    @Test
    @DisplayName("연도 필터 없이 전체 조회 — 저장한 기간이 포함된다")
    void findEvaluationPeriods_noFilter() {
        // given
        insertPeriod(2026, 1, "IN_PROGRESS");
        insertPeriod(2025, 1, "CONFIRMED");

        // when
        List<EvaluationPeriodSummaryResponse> result = mapper.findEvaluationPeriods(null, null, 10, 0);

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("연도 필터 적용 — 해당 연도 기간만 반환")
    void findEvaluationPeriods_withYearFilter() {
        // given
        insertPeriod(2026, 1, "IN_PROGRESS");
        insertPeriod(2025, 1, "CONFIRMED");

        // when
        List<EvaluationPeriodSummaryResponse> result = mapper.findEvaluationPeriods(2026, null, 10, 0);

        // then
        assertThat(result).allMatch(r -> r.getEvalYear() == 2026);
    }

    @Test
    @DisplayName("상태 필터 적용 — 해당 상태 기간만 반환")
    void findEvaluationPeriods_withStatusFilter() {
        // given
        insertPeriod(2026, 1, "IN_PROGRESS");
        insertPeriod(2026, 2, "CONFIRMED");

        // when
        List<EvaluationPeriodSummaryResponse> result =
                mapper.findEvaluationPeriods(null, "IN_PROGRESS", 10, 0);

        // then
        assertThat(result).allMatch(r -> r.getStatus().equals("IN_PROGRESS"));
    }

    /* ── countEvaluationPeriods ───────────────────────────────────────── */

    @Test
    @DisplayName("연도 필터로 카운트 — 해당 연도 건수만 반환")
    void countEvaluationPeriods_withYearFilter() {
        // given
        insertPeriod(2026, 1, "IN_PROGRESS");
        insertPeriod(2026, 2, "CONFIRMED");
        insertPeriod(2025, 1, "CONFIRMED");

        // when
        long count = mapper.countEvaluationPeriods(2026, null);

        // then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    /* ── findCurrentDeadline ──────────────────────────────────────────── */

    @Test
    @DisplayName("현재 마감일 조회 — IN_PROGRESS 기간이 있으면 반환")
    void findCurrentDeadline_success() {
        // given
        insertPeriod(2026, 1, "IN_PROGRESS");

        // when
        EvaluationPeriodDeadlineResponse result = mapper.findCurrentDeadline();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEvalPeriodId()).isNotNull();
    }

    @Test
    @DisplayName("현재 마감일 조회 — IN_PROGRESS 기간 없으면 null 반환")
    void findCurrentDeadline_noInProgress() {
        // given
        insertPeriod(2025, 1, "CONFIRMED");

        // when
        EvaluationPeriodDeadlineResponse result = mapper.findCurrentDeadline();

        // then — null 반환 → 서비스에서 예외 처리
        assertThat(result).isNull();
    }

    /* ── findCurrentPeriodId ──────────────────────────────────────────── */

    @Test
    @DisplayName("현재 기간 ID 조회 — IN_PROGRESS 기간이 있으면 ID 반환")
    void findCurrentPeriodId_success() {
        // given
        long periodId = insertPeriod(2026, 1, "IN_PROGRESS");

        // when
        Long result = mapper.findCurrentPeriodId();

        // then
        assertThat(result).isEqualTo(periodId);
    }

    @Test
    @DisplayName("현재 기간 ID 조회 — IN_PROGRESS 기간 없으면 null 반환")
    void findCurrentPeriodId_noInProgress() {
        // given
        insertPeriod(2025, 1, "CONFIRMED");

        // when
        Long result = mapper.findCurrentPeriodId();

        // then — null 반환 → 서비스에서 예외 처리
        assertThat(result).isNull();
    }

    /* ── findTlTargets / findDlTargets ────────────────────────────────── */

    @Test
    @DisplayName("TL 평가 대상 조회 — 매칭되는 데이터 없으면 빈 목록 반환")
    void findTlTargets_emptyWhenNoData() {
        // given — 존재하지 않는 TL ID와 기간 ID

        // when
        var result = mapper.findTlTargets(999L, 999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("DL 평가 대상 조회 — 매칭되는 데이터 없으면 빈 목록 반환")
    void findDlTargets_emptyWhenNoData() {
        // given — 존재하지 않는 DL ID와 기간 ID

        // when
        var result = mapper.findDlTargets(999L, 999L);

        // then
        assertThat(result).isEmpty();
    }
}
