package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalHistoryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalHistoryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalStatusResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerFeedbackItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerFeedbackResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerQualitativeResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerQuantitativeResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.WorkerEvaluationQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkerEvaluationQueryService {

    private final WorkerEvaluationQueryMapper mapper;

    /** HR-EVAL-007: 현재 진행 중인 기간의 평가 완료 여부·분기 정보 */
    public WorkerEvalStatusResponse getEvalStatus(Long employeeId) {
        WorkerEvalStatusResponse result = mapper.findCurrentEvalStatus(employeeId);
        if (result == null) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_NOT_IN_PROGRESS);
        }
        return result;
    }

    /** HR-EVAL-008: 내 정량 평가 점수·항목별 상세 */
    public WorkerQuantitativeResponse getQuantitative(Long employeeId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        WorkerQuantitativeResponse result = mapper.findQuantitative(employeeId, resolvedPeriodId);
        if (result == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return result;
    }

    /** HR-EVAL-009: 내 정성 평가 카테고리별 점수 (level 3, HRM 확정본) */
    public WorkerQualitativeResponse getQualitative(Long employeeId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        WorkerQualitativeResponse result = mapper.findQualitative(employeeId, resolvedPeriodId);
        if (result == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return result;
    }

    /** HR-EVAL-010: 분기별 성장 피드백·코멘트 (TL/DL/HRM 각 레벨) */
    public WorkerFeedbackResponse getFeedback(Long employeeId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        List<WorkerFeedbackItem> items = mapper.findFeedbackItems(employeeId, resolvedPeriodId);

        // period 정보는 mapper에서 가져온 items에 없으므로 별도 조회 없이 resolvedPeriodId만 세팅
        // (evalYear, evalSequence는 프론트가 periodId로 관리 — 필요시 join 확장)
        WorkerFeedbackResponse response = new WorkerFeedbackResponse();
        response.setEvalPeriodId(resolvedPeriodId);
        response.setFeedbackItems(items);
        return response;
    }

    /** HR-EVAL-011: 평가 이력 목록 (이의신청 대상 선택용, 페이징) */
    public WorkerEvalHistoryResponse getEvalHistory(Long employeeId, int page, int size) {
        int offset = (page - 1) * size;
        List<WorkerEvalHistoryItem> content = mapper.findEvalHistory(employeeId, size, offset);
        long totalCount = mapper.countEvalHistory(employeeId);
        return new WorkerEvalHistoryResponse(content, totalCount);
    }

    private Long resolvePeriodId(Long periodId) {
        if (periodId != null) return periodId;
        Long currentId = mapper.findCurrentPeriodId();
        if (currentId == null) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_NOT_IN_PROGRESS);
        }
        return currentId;
    }
}
