package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EvaluationPeriodRepositoryTest {

    @Autowired
    private EvaluationPeriodRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("DELETE FROM qualitative_evaluation");
        jdbcTemplate.execute("DELETE FROM evaluation_period");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private EvaluationPeriod buildPeriod(int year, EvalPeriodStatus status) {
        return EvaluationPeriod.builder()
                .evalPeriodId(idGenerator.generate())
                .algorithmVersionId(1L)
                .evalYear(year)
                .evalSequence(1)
                .startDate(LocalDate.of(year, 1, 1))
                .endDate(LocalDate.of(year, 3, 31))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("특정 상태의 평가 기간이 존재하면 true를 반환한다")
    void existsByStatus_true() {
        // given
        repository.save(buildPeriod(2026, EvalPeriodStatus.IN_PROGRESS));

        // when & then
        assertThat(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).isTrue();
    }

    @Test
    @DisplayName("특정 상태의 평가 기간이 없으면 false를 반환한다")
    void existsByStatus_false() {
        // given & when & then
        assertThat(repository.existsByStatus(EvalPeriodStatus.CLOSING)).isFalse();
    }
}
