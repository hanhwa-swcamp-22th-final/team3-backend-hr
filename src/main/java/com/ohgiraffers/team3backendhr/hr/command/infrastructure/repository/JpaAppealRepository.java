package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.EvaluationAppeal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAppealRepository extends JpaRepository<EvaluationAppeal, Long> {

    boolean existsByAppealEmployeeIdAndEvaluationPeriodId(Long appealEmployeeId, Long evaluationPeriodId);
}
