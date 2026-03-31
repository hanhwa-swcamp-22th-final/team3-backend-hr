package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvaluationPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/disable-fk.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class EvaluationPeriodRepositoryTest {

    @Autowired
    private EvaluationPeriodRepository repository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private EvaluationPeriod buildPeriod(int year, EvalPeriodStatus status) {
        return EvaluationPeriod.builder()
                .evalPeriodId(idGenerator.generate())
                .algorithmVersionId(1L)
                .evalYear(year)
                .evalSequence(1)
                .evalType(EvalType.QUALITATIVE)
                .startDate(LocalDate.of(year, 1, 1))
                .endDate(LocalDate.of(year, 3, 31))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("평가 기간을 저장한다")
    void save_success() {
        EvaluationPeriod period = buildPeriod(2026, EvalPeriodStatus.IN_PROGRESS);

        EvaluationPeriod saved = repository.save(period);

        assertThat(saved.getEvalPeriodId()).isEqualTo(period.getEvalPeriodId());
        assertThat(saved.getEvalYear()).isEqualTo(2026);
        assertThat(saved.getStatus()).isEqualTo(EvalPeriodStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("ID로 평가 기간을 조회한다")
    void findById_success() {
        EvaluationPeriod period = repository.save(buildPeriod(2026, EvalPeriodStatus.IN_PROGRESS));

        Optional<EvaluationPeriod> result = repository.findById(period.getEvalPeriodId());

        assertThat(result).isPresent();
        assertThat(result.get().getEvalPeriodId()).isEqualTo(period.getEvalPeriodId());
        assertThat(result.get().getEvalYear()).isEqualTo(2026);
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 empty를 반환한다")
    void findById_whenNotFound_thenEmpty() {
        Optional<EvaluationPeriod> result = repository.findById(-1L);

        assertThat(result).isEmpty();
    }
}
