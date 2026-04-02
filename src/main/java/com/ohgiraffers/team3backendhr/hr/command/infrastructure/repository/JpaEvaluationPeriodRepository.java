package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvaluationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEvaluationPeriodRepository extends JpaRepository<EvaluationPeriod, Long> {

    boolean existsByStatus(EvalPeriodStatus status);
}
