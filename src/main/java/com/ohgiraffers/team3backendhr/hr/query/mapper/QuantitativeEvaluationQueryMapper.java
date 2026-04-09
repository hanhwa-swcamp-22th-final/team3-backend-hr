package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation.QuantitativeEvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation.QuantitativeEvaluationSummaryItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuantitativeEvaluationQueryMapper {

    List<QuantitativeEvaluationSummaryItem> findList(
            @Param("periodId") Long periodId,
            @Param("status") String status,
            @Param("size") int size,
            @Param("offset") int offset);

    long countList(
            @Param("periodId") Long periodId,
            @Param("status") String status);

    QuantitativeEvaluationDetailResponse findById(@Param("evaluationId") Long evaluationId);
}
