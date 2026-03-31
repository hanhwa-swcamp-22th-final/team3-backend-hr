package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql(scripts = "/disable-fk.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class EvaluationPeriodQueryMapperTest {

    @Autowired
    private EvaluationPeriodQueryMapper mapper;

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
    @DisplayName("연도로 평가 기간 목록을 조회한다")
    void findByEvalYear_success() {
        repository.saveAndFlush(buildPeriod(2026, EvalPeriodStatus.IN_PROGRESS));
        repository.saveAndFlush(buildPeriod(2026, EvalPeriodStatus.CONFIRMED));
        repository.saveAndFlush(buildPeriod(2025, EvalPeriodStatus.CONFIRMED));

        List<EvaluationPeriod> result = mapper.findByEvalYear(2026);

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).allMatch(p -> p.getEvalYear() == 2026);
    }

    @Test
    @DisplayName("상태로 평가 기간을 조회한다")
    void findByStatus_success() {
        repository.saveAndFlush(buildPeriod(2026, EvalPeriodStatus.IN_PROGRESS));

        EvaluationPeriod result = mapper.findByStatus(EvalPeriodStatus.IN_PROGRESS.name());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(EvalPeriodStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("특정 상태의 평가 기간이 존재하는지 확인한다")
    void existsByStatus_success() {
        repository.saveAndFlush(buildPeriod(2026, EvalPeriodStatus.IN_PROGRESS));

        assertThat(mapper.existsByStatus(EvalPeriodStatus.IN_PROGRESS.name())).isTrue();
        assertThat(mapper.existsByStatus(EvalPeriodStatus.CLOSING.name())).isFalse();
    }
}
