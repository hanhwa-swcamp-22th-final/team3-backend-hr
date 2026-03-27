package com.ohgiraffers.team3backendhr.evaluation.command.domain.repository;

import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvalType;
import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvaluationPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/disable-fk.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class EvaluationPeriodRepositoryTest {

    @Autowired
    private EvaluationPeriodRepository repository;

    private EvaluationPeriod buildPeriod(int year, int seq, EvalPeriodStatus status) {
        return EvaluationPeriod.builder()
                .algorithmVersionId(1L)
                .evalYear(year)
                .evalSequence(seq)
                .evalType(EvalType.QUALITATIVE)
                .startDate(LocalDate.of(year, 1, 1))
                .endDate(LocalDate.of(year, 3, 31))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("연도로 평가 기간 목록을 조회한다")
    void findByEvalYear_success() {
        repository.save(buildPeriod(2026, 1, EvalPeriodStatus.IN_PROGRESS));
        repository.save(buildPeriod(2026, 2, EvalPeriodStatus.CONFIRMED));
        repository.save(buildPeriod(2025, 1, EvalPeriodStatus.CONFIRMED));

        List<EvaluationPeriod> result = repository.findByEvalYear(2026);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getEvalYear() == 2026);
    }

    @Test
    @DisplayName("상태로 평가 기간을 조회한다")
    void findByStatus_success() {
        repository.save(buildPeriod(2026, 1, EvalPeriodStatus.IN_PROGRESS));

        Optional<EvaluationPeriod> result = repository.findByStatus(EvalPeriodStatus.IN_PROGRESS);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(EvalPeriodStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("특정 상태의 평가 기간이 존재하는지 확인한다")
    void existsByStatus_success() {
        repository.save(buildPeriod(2026, 1, EvalPeriodStatus.IN_PROGRESS));

        assertThat(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).isTrue();
        assertThat(repository.existsByStatus(EvalPeriodStatus.CLOSING)).isFalse();
    }
}
