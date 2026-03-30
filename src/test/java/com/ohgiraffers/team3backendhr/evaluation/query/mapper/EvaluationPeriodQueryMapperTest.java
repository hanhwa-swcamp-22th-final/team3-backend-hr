package com.ohgiraffers.team3backendhr.evaluation.query.mapper;

import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvalType;
import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.evaluation.command.domain.repository.EvaluationPeriodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EvaluationPeriodQueryMapperTest {

    @Autowired
    private EvaluationPeriodQueryMapper mapper;

    @Autowired
    private EvaluationPeriodRepository repository;

    private EvaluationPeriod buildPeriod(int year, int seq, EvalPeriodStatus status) {
        return EvaluationPeriod.builder()
                .evalPeriodId(System.currentTimeMillis() * 1000 + seq)
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

        List<EvaluationPeriod> result = mapper.findByEvalYear(2026);

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).allMatch(p -> p.getEvalYear() == 2026);
    }

    @Test
    @DisplayName("상태로 평가 기간을 조회한다")
    void findByStatus_success() {
        repository.save(buildPeriod(2026, 1, EvalPeriodStatus.IN_PROGRESS));

        EvaluationPeriod result = mapper.findByStatus(EvalPeriodStatus.IN_PROGRESS.name());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(EvalPeriodStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("특정 상태의 평가 기간이 존재하는지 확인한다")
    void existsByStatus_success() {
        repository.save(buildPeriod(2026, 1, EvalPeriodStatus.IN_PROGRESS));

        assertThat(mapper.existsByStatus(EvalPeriodStatus.IN_PROGRESS.name())).isTrue();
        assertThat(mapper.existsByStatus(EvalPeriodStatus.CLOSING.name())).isFalse();
    }
}
