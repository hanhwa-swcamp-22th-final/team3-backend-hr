package com.ohgiraffers.team3backendhr.evaluation.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvaluationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEvaluationPeriodRepository extends JpaRepository<EvaluationPeriod, Long> {
}
