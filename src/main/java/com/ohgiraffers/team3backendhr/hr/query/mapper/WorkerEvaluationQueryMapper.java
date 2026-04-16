package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalHistoryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalReviewItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalStatusResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerQualitativeResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerQuantitativeResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkerEvaluationQueryMapper {

    /** HR-EVAL-007: 현재 진행 중인 기간, 없으면 최근 확정 기간의 내 평가 완료 여부·분기 정보 */
    WorkerEvalStatusResponse findPreferredEvalStatus(@Param("employeeId") Long employeeId);

    /** HR-EVAL-008: 내 정량 평가 점수·항목별 상세 */
    WorkerQuantitativeResponse findQuantitative(
            @Param("employeeId") Long employeeId,
            @Param("periodId") Long periodId);

    /** HR-EVAL-009: 내 정성 평가 카테고리별 점수 (level 3, HRM 확정본) */
    WorkerQualitativeResponse findQualitative(
            @Param("employeeId") Long employeeId,
            @Param("periodId") Long periodId);

    /** HR-EVAL-NEW: 이의신청용 1차(TL)·2차(DL) 평가 결과 (SUBMITTED 상태만) */
    List<WorkerEvalReviewItem> findEvalReviewItems(
            @Param("employeeId") Long employeeId,
            @Param("periodId") Long periodId);

    /** HR-EVAL-011: 평가 이력 목록 (페이징) */
    List<WorkerEvalHistoryItem> findEvalHistory(
            @Param("employeeId") Long employeeId,
            @Param("size") int size,
            @Param("offset") int offset);

    long countEvalHistory(@Param("employeeId") Long employeeId);

    /** 현재 IN_PROGRESS 기간, 없으면 최근 CONFIRMED 기간 ID */
    Long findPreferredPeriodId();
}
