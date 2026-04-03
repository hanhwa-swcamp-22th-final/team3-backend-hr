package com.ohgiraffers.team3backendhr.appeal.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.appeal.command.domain.aggregate.EvaluationAppeal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAppealRepository extends JpaRepository<EvaluationAppeal, Long> {
}
