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
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QualitativeEvaluationQueryMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QualitativeEvaluationQueryService {

    private final QualitativeEvaluationQueryMapper mapper;
    private final EvaluationPeriodRepository evaluationPeriodRepository;

    /* TL target list for level 1 qualitative evaluation */
    public TlEvaluationTargetResponse getTlTargets(Long tlId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        List<TlEvaluationTargetItem> targets = mapper.findTlTargets(tlId, resolvedPeriodId);
        EvaluationPeriod period = evaluationPeriodRepository.findById(resolvedPeriodId).orElse(null);
        return new TlEvaluationTargetResponse(
            resolvedPeriodId,
            List.of(resolvedPeriodId),
            targets,
            period != null ? period.getEvalYear() : null,
            period != null ? period.getEvalSequence() : null,
            period != null ? period.getStartDate() : null,
            period != null ? period.getEndDate() : null,
            period != null && period.getStatus() != null ? period.getStatus().name() : null
        );
    }

    /* DL target list for level 2 qualitative evaluation */
    public DlEvaluationTargetResponse getDlTargets(Long dlId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        List<DlEvaluationTargetItem> targets = mapper.findDlTargets(dlId, resolvedPeriodId);
        EvaluationPeriod period = evaluationPeriodRepository.findById(resolvedPeriodId).orElse(null);
        return new DlEvaluationTargetResponse(
            resolvedPeriodId,
            targets,
            period != null ? period.getEvalYear() : null,
            period != null ? period.getEvalSequence() : null,
            period != null ? period.getStartDate() : null,
            period != null ? period.getEndDate() : null,
            period != null && period.getStatus() != null ? period.getStatus().name() : null
        );
    }

    /* TL evaluation detail */
    public EvaluationDetailResponse getTlEvaluationDetail(Long tlId, Long evalId) {
        EvaluationDetailResponse detail = mapper.findTlEvaluationDetail(evalId, tlId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return detail;
    }

    /* DL evaluation detail */
    public DlEvaluationDetailResponse getDlEvaluationDetail(Long dlId, Long evaluateeId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        DlEvaluationDetailResponse detail = mapper.findDlEvaluationDetail(dlId, evaluateeId, resolvedPeriodId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return detail;
    }

    /* HRM evaluation detail */
    public EvaluationDetailResponse getEvaluationDetail(Long evalId) {
        EvaluationDetailResponse detail = mapper.findEvaluationDetail(evalId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return detail;
    }

    /* HRM evaluation grade summary */
    public List<EvaluationGradeSummaryItem> getEvaluationGradeSummary(Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        return mapper.findEvaluationGradeSummary(resolvedPeriodId);
    }

    /* HRM evaluation list with filters */
    public EvaluationListResponse getEvaluations(Long periodId, String grade, String status, int page, int size) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        int offset = page * size;
        List<EvaluationSummaryItem> content = mapper.findEvaluations(resolvedPeriodId, grade, status, size, offset);
        long totalCount = mapper.countEvaluations(resolvedPeriodId, grade, status);
        long totalPages = (long) Math.ceil((double) totalCount / size);
        return new EvaluationListResponse(content, totalCount, totalPages);
    }

    /* Resolve current in-progress period when periodId is omitted */
    private Long resolvePeriodId(Long periodId) {
        if (periodId != null) return periodId;
        Long currentId = mapper.findCurrentPeriodId();
        if (currentId == null) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_NOT_IN_PROGRESS);
        }
        return currentId;
    }
}
