package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EvaluationPeriodQueryMapperTest {

    @Autowired
    private EvaluationPeriodQueryMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private void insertPeriod(int year, String status) {
        jdbcTemplate.update("""
                INSERT INTO evaluation_period
                  (eval_period_id, algorithm_version_id, eval_year, eval_sequence,
                   eval_type, start_date, end_date, status)
                VALUES (?, 1, ?, 1, 'QUALITATIVE', ?, ?, ?)
                """,
                idGenerator.generate(), year,
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 3, 31),
                status);
    }

    /* ── findByEvalYear ──────────────────────────────────────────────── */

    @Test
    @DisplayName("연도로 평가 기간 목록을 조회한다")
    void findByEvalYear_success() {
        // given
        insertPeriod(2026, "IN_PROGRESS");
        insertPeriod(2026, "CONFIRMED");
        insertPeriod(2025, "CONFIRMED");

        // when
        List<EvaluationPeriod> result = mapper.findByEvalYear(2026);

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).allMatch(p -> p.getEvalYear() == 2026);
    }

    /* ── findByStatus ────────────────────────────────────────────────── */

    @Test
    @DisplayName("상태로 평가 기간을 조회한다")
    void findByStatus_success() {
        // given
        insertPeriod(2026, "IN_PROGRESS");

        // when
        EvaluationPeriod result = mapper.findByStatus("IN_PROGRESS");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus().name()).isEqualTo("IN_PROGRESS");
    }
}
