package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvaluationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface JpaEvaluationPeriodRepository extends JpaRepository<EvaluationPeriod, Long> {

    boolean existsByStatus(EvalPeriodStatus status);

    boolean existsByEvalYearAndEvalSequenceAndEvalType(int evalYear, int evalSequence, EvalType evalType);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndEvalPeriodIdNot(
            LocalDate endDate, LocalDate startDate, Long evalPeriodId);
}
