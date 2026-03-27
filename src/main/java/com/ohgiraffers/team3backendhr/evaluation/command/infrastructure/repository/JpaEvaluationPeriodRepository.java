package com.ohgiraffers.team3backendhr.evaluation.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvaluationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaEvaluationPeriodRepository extends JpaRepository<EvaluationPeriod, Long> {

    List<EvaluationPeriod> findByEvalYear(Integer evalYear);

    Optional<EvaluationPeriod> findByStatus(EvalPeriodStatus status);

    boolean existsByStatus(EvalPeriodStatus status);
}
