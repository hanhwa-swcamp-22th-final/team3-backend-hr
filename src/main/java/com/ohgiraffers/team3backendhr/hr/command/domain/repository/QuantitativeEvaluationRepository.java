package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantitativeEvaluation;

import java.util.Optional;

public interface QuantitativeEvaluationRepository {

    QuantitativeEvaluation save(QuantitativeEvaluation evaluation);

    Optional<QuantitativeEvaluation> findById(Long id);

    Optional<QuantitativeEvaluation> findByEmployeeIdAndEvalPeriodId(Long employeeId, Long evalPeriodId);
}
