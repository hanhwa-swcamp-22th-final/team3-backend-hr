package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantitativeEvaluation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaQuantitativeEvaluationRepository extends JpaRepository<QuantitativeEvaluation, Long> {

    Optional<QuantitativeEvaluation> findByEmployeeIdAndEvaluationPeriodIdAndEquipmentId(
        Long employeeId,
        Long evaluationPeriodId,
        Long equipmentId
    );
}
