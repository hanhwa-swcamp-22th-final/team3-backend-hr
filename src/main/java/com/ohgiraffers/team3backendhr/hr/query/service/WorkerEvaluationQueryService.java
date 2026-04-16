package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalHistoryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalHistoryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalReviewItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalReviewResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerEvalStatusResponse;
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
        WorkerEvalStatusResponse result = mapper.findPreferredEvalStatus(employeeId);
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

    /** HR-EVAL-NEW: 이의신청용 1차·2차 평가 결과 조회 */
    public WorkerEvalReviewResponse getEvalReview(Long employeeId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        List<WorkerEvalReviewItem> items = mapper.findEvalReviewItems(employeeId, resolvedPeriodId);

        WorkerEvalReviewItem firstEval = items.stream()
                .filter(i -> i.getEvaluationLevel() == 1)
                .findFirst().orElse(null);
        WorkerEvalReviewItem secondEval = items.stream()
                .filter(i -> i.getEvaluationLevel() == 2)
                .findFirst().orElse(null);

        // period 정보는 어느 item이든 동일하므로 첫 번째 것에서 추출
        WorkerEvalReviewItem any = items.isEmpty() ? null : items.get(0);
        return new WorkerEvalReviewResponse(
                any != null ? any.getEvalPeriodId() : null,
                any != null ? any.getEvalYear() : null,
                any != null ? any.getEvalSequence() : null,
                firstEval,
                secondEval,
                firstEval != null && secondEval != null
        );
    }

    /** HR-EVAL-011: 평가 이력 목록 (이의신청 대상 선택용, 페이징) */
    public WorkerEvalHistoryResponse getEvalHistory(Long employeeId, int page, int size) {
        int offset = page * size;
        List<WorkerEvalHistoryItem> content = mapper.findEvalHistory(employeeId, size, offset);
        long totalCount = mapper.countEvalHistory(employeeId);
        return new WorkerEvalHistoryResponse(content, totalCount);
    }

    private Long resolvePeriodId(Long periodId) {
        if (periodId != null) return periodId;
        Long currentId = mapper.findPreferredPeriodId();
        if (currentId == null) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_NOT_IN_PROGRESS);
        }
        return currentId;
    }
}
