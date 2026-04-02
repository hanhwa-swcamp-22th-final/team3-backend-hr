package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.DlEvaluationTargetItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.EvaluationPeriodDeadlineResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.EvaluationPeriodSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.TlEvaluationTargetItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QualitativeEvaluationQueryMapper {

    /* 평가 기간 목록 조회 (HRM) — year/status 필터, 페이징 */
    List<EvaluationPeriodSummaryResponse> findEvaluationPeriods(
            @Param("year") Integer year,
            @Param("status") String status,
            @Param("size") int size,
            @Param("offset") int offset);

    long countEvaluationPeriods(
            @Param("year") Integer year,
            @Param("status") String status);

    /* 현재 IN_PROGRESS 기간 마감일 조회 (TL, DL) */
    EvaluationPeriodDeadlineResponse findCurrentDeadline();

    /* TL 평가 대상 목록 — 같은 부서 WORKER × level 1 레코드 */
    List<TlEvaluationTargetItem> findTlTargets(
            @Param("tlId") Long tlId,
            @Param("periodId") Long periodId);

    /* DL 평가 대상 목록 — 같은 부서, level 1 SUBMITTED인 레코드 × level 2 */
    List<DlEvaluationTargetItem> findDlTargets(
            @Param("dlId") Long dlId,
            @Param("periodId") Long periodId);

    /* periodId 미입력 시 현재 IN_PROGRESS 기간 ID 조회 */
    Long findCurrentPeriodId();
}
