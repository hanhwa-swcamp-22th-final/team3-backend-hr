package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualitativeEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaQualitativeEvaluationRepository extends JpaRepository<QualitativeEvaluation, Long> {

    Optional<QualitativeEvaluation> findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
            Long evaluateeId, Long evaluationPeriodId, Long evaluationLevel);
}
