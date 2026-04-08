package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationTargetItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationTargetResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationGradeSummaryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationSummaryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.TlEvaluationTargetItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.TlEvaluationTargetResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QualitativeEvaluationQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QualitativeEvaluationQueryService {

    private final QualitativeEvaluationQueryMapper mapper;

    /* TL 평가 대상 조회 — 같은 부서 WORKER × level 1 레코드 반환 */
    public TlEvaluationTargetResponse getTlTargets(Long tlId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        List<TlEvaluationTargetItem> targets = mapper.findTlTargets(tlId, resolvedPeriodId);
        return new TlEvaluationTargetResponse(resolvedPeriodId, targets);
    }

    /* DL 평가 대상 조회 — level 1 SUBMITTED인 대상 × level 2 레코드 반환 */
    public DlEvaluationTargetResponse getDlTargets(Long dlId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        List<DlEvaluationTargetItem> targets = mapper.findDlTargets(dlId, resolvedPeriodId);
        return new DlEvaluationTargetResponse(resolvedPeriodId, targets);
    }

    /* TL — 제출 완료 평가 상세 조회 (본인 제출분만) */
    public EvaluationDetailResponse getTlEvaluationDetail(Long tlId, Long evalId) {
        EvaluationDetailResponse detail = mapper.findTlEvaluationDetail(evalId, tlId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return detail;
    }

    /* DL — 1차 평가 항목 + AI 추천 점수 조회 (본인 부서 직원만) */
    public DlEvaluationDetailResponse getDlEvaluationDetail(Long dlId, Long evaluateeId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        DlEvaluationDetailResponse detail = mapper.findDlEvaluationDetail(dlId, evaluateeId, resolvedPeriodId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return detail;
    }

    /* HRM — 평가 상세 조회 */
    public EvaluationDetailResponse getEvaluationDetail(Long evalId) {
        EvaluationDetailResponse detail = mapper.findEvaluationDetail(evalId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return detail;
    }

    /* HRM — 등급별 평가 집계 */
    public List<EvaluationGradeSummaryItem> getEvaluationGradeSummary(Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        return mapper.findEvaluationGradeSummary(resolvedPeriodId);
    }

    /* HRM — 평가 목록 조회 (periodId·grade·status 필터, 페이징) */
    public EvaluationListResponse getEvaluations(Long periodId, String grade, String status, int page, int size) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        int offset = (page - 1) * size;
        List<EvaluationSummaryItem> content = mapper.findEvaluations(resolvedPeriodId, grade, status, size, offset);
        long totalCount = mapper.countEvaluations(resolvedPeriodId, grade, status);
        long totalPages = (long) Math.ceil((double) totalCount / size);
        return new EvaluationListResponse(content, totalCount, totalPages);
    }

    /* periodId null 이면 현재 IN_PROGRESS 기간으로 자동 resolve — 프론트에서 기간 선택 안 해도 동작 */
    private Long resolvePeriodId(Long periodId) {
        if (periodId != null) return periodId;
        Long currentId = mapper.findCurrentPeriodId();
        if (currentId == null) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_NOT_IN_PROGRESS);
        }
        return currentId;
    }
}
