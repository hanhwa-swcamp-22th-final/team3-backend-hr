package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcriteria.EvaluationCategory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcriteria.EvaluationTierGroup;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcriteria.EvaluationWeightConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEvaluationWeightConfigRepository extends JpaRepository<EvaluationWeightConfig, Long> {

    Optional<EvaluationWeightConfig> findByTierGroupAndCategoryCodeAndActiveTrueAndDeletedFalse(
        EvaluationTierGroup tierGroup,
        EvaluationCategory categoryCode
    );

    Optional<EvaluationWeightConfig> findTopByTierGroupAndCategoryCodeOrderByCreatedAtDescEvaluationWeightConfigIdDesc(
        EvaluationTierGroup tierGroup,
        EvaluationCategory categoryCode
    );

    List<EvaluationWeightConfig> findAllByActiveTrueAndDeletedFalse();
}
