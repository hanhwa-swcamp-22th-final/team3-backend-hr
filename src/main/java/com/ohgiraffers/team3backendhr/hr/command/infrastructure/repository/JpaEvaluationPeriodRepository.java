package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface JpaEvaluationPeriodRepository extends JpaRepository<EvaluationPeriod, Long> {

    boolean existsByStatus(EvalPeriodStatus status);

    boolean existsByEvalYearAndEvalSequence(int evalYear, int evalSequence);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndEvalPeriodIdNot(
            LocalDate endDate, LocalDate startDate, Long evalPeriodId);
}
