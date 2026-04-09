package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.KpiMemberDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.KpiMemberSummaryResponse;
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

    /* HR-010: 팀원별 정량 점수 요약 (설비별 집계) */
    List<KpiMemberSummaryResponse> findTeamKpiSummary(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("year") int year,
            @Param("quarter") int quarter);

    /* HR-011: 특정 팀원 정량 점수 상세 (설비별 행 반환) */
    List<KpiMemberDetailResponse> findMemberKpiDetail(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("quarter") int quarter);
}
