package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationTargetItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationperiod.EvaluationPeriodDeadlineResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationperiod.EvaluationPeriodSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationGradeSummaryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationSummaryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.TlEvaluationTargetItem;
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

    /* HRM — 평가 목록 조회 (periodId·grade·status 필터, 페이징) */
    List<EvaluationSummaryItem> findEvaluations(
            @Param("periodId") Long periodId,
            @Param("grade") String grade,
            @Param("status") String status,
            @Param("size") int size,
            @Param("offset") int offset);

    long countEvaluations(
            @Param("periodId") Long periodId,
            @Param("grade") String grade,
            @Param("status") String status);

    /* TL — 제출 완료 평가 상세 조회 (본인 제출분만) */
    EvaluationDetailResponse findTlEvaluationDetail(
            @Param("evalId") Long evalId,
            @Param("tlId") Long tlId);

    /* DL — 1차 평가 항목 + AI 추천 점수 조회 */
    DlEvaluationDetailResponse findDlEvaluationDetail(
            @Param("evaluateeId") Long evaluateeId,
            @Param("periodId") Long periodId);

    /* HRM — 평가 상세 조회 */
    EvaluationDetailResponse findEvaluationDetail(@Param("evalId") Long evalId);

    /* HRM — 등급별 평가 집계 */
    List<EvaluationGradeSummaryItem> findEvaluationGradeSummary(@Param("periodId") Long periodId);
}
