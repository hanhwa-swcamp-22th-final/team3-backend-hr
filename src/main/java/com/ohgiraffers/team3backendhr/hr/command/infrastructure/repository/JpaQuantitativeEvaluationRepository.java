package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QuantitativeEvaluationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaQuantitativeEvaluationRepository
        extends JpaRepository<QuantitativeEvaluation, Long>, QuantitativeEvaluationRepository {

    Optional<QuantitativeEvaluation> findByEmployeeIdAndEvalPeriodId(Long employeeId, Long evalPeriodId);
}
