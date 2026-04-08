package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EvaluationPeriodQueryMapper {

    EvaluationPeriod findByStatus(@Param("status") String status);

    List<EvaluationPeriod> findByEvalYear(@Param("evalYear") Integer evalYear);
}
