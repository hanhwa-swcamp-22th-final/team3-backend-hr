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

    /* TL ?됯? ???議고쉶 ??媛숈? 遺??WORKER 횞 level 1 ?덉퐫??諛섑솚 */
    public TlEvaluationTargetResponse getTlTargets(Long tlId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        List<TlEvaluationTargetItem> targets = mapper.findTlTargets(tlId, resolvedPeriodId);
        return new TlEvaluationTargetResponse(resolvedPeriodId, List.of(resolvedPeriodId), targets);
    }

    /* DL ?됯? ???議고쉶 ??level 1 SUBMITTED?????횞 level 2 ?덉퐫??諛섑솚 */
    public DlEvaluationTargetResponse getDlTargets(Long dlId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        List<DlEvaluationTargetItem> targets = mapper.findDlTargets(dlId, resolvedPeriodId);
        return new DlEvaluationTargetResponse(resolvedPeriodId, targets);
    }

    /* TL ???쒖텧 ?꾨즺 ?됯? ?곸꽭 議고쉶 (蹂몄씤 ?쒖텧遺꾨쭔) */
    public EvaluationDetailResponse getTlEvaluationDetail(Long tlId, Long evalId) {
        EvaluationDetailResponse detail = mapper.findTlEvaluationDetail(evalId, tlId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return detail;
    }

    /* DL ??1李??됯? ??ぉ + AI 異붿쿇 ?먯닔 議고쉶 (蹂몄씤 遺??吏곸썝留? */
    public DlEvaluationDetailResponse getDlEvaluationDetail(Long dlId, Long evaluateeId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        DlEvaluationDetailResponse detail = mapper.findDlEvaluationDetail(dlId, evaluateeId, resolvedPeriodId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return detail;
    }

    /* HRM ???됯? ?곸꽭 議고쉶 */
    public EvaluationDetailResponse getEvaluationDetail(Long evalId) {
        EvaluationDetailResponse detail = mapper.findEvaluationDetail(evalId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return detail;
    }

    /* HRM ???깃툒蹂??됯? 吏묎퀎 */
    public List<EvaluationGradeSummaryItem> getEvaluationGradeSummary(Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        return mapper.findEvaluationGradeSummary(resolvedPeriodId);
    }

    /* HRM ???됯? 紐⑸줉 議고쉶 (periodId쨌grade쨌status ?꾪꽣, ?섏씠吏? */
    public EvaluationListResponse getEvaluations(Long periodId, String grade, String status, int page, int size) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        int offset = page * size;
        List<EvaluationSummaryItem> content = mapper.findEvaluations(resolvedPeriodId, grade, status, size, offset);
        long totalCount = mapper.countEvaluations(resolvedPeriodId, grade, status);
        long totalPages = (long) Math.ceil((double) totalCount / size);
        return new EvaluationListResponse(content, totalCount, totalPages);
    }

    /* periodId null ?대㈃ ?꾩옱 IN_PROGRESS 湲곌컙?쇰줈 ?먮룞 resolve ???꾨줎?몄뿉??湲곌컙 ?좏깮 ???대룄 ?숈옉 */
    private Long resolvePeriodId(Long periodId) {
        if (periodId != null) return periodId;
        Long currentId = mapper.findCurrentPeriodId();
        if (currentId == null) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_NOT_IN_PROGRESS);
        }
        return currentId;
    }
}
