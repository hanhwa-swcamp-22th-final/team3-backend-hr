package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcomment.EvaluationComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEvaluationCommentRepository extends JpaRepository<EvaluationComment, Long> {

    void deleteByQualitativeEvaluationId(Long qualitativeEvaluationId);
}