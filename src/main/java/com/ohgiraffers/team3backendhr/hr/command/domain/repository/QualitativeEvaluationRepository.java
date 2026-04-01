package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualitativeEvaluation;

import java.util.List;
import java.util.Optional;

public interface QualitativeEvaluationRepository {

    QualitativeEvaluation save(QualitativeEvaluation qualitativeEvaluation);

    List<QualitativeEvaluation> saveAll(List<QualitativeEvaluation> evaluations);

    Optional<QualitativeEvaluation> findById(Long qualitativeEvaluationId);

    Optional<QualitativeEvaluation> findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
            Long evaluateeId, Long evaluationPeriodId, Long evaluationLevel);
}
