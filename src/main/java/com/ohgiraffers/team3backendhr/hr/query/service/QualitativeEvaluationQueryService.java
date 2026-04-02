package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.DlEvaluationTargetItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.TlEvaluationTargetItem;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QualitativeEvaluationQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QualitativeEvaluationQueryService {

    private final QualitativeEvaluationQueryMapper mapper;

    /* TL 평가 대상 조회 — 같은 부서 WORKER × level 1 레코드 반환 */
    public Map<String, Object> getTlTargets(Long tlId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        List<TlEvaluationTargetItem> targets = mapper.findTlTargets(tlId, resolvedPeriodId);

        Map<String, Object> result = new HashMap<>();
        result.put("evalPeriodId", resolvedPeriodId);
        result.put("targets", targets);
        return result;
    }

    /* DL 평가 대상 조회 — level 1 SUBMITTED인 대상 × level 2 레코드 반환 */
    public Map<String, Object> getDlTargets(Long dlId, Long periodId) {
        Long resolvedPeriodId = resolvePeriodId(periodId);
        List<DlEvaluationTargetItem> targets = mapper.findDlTargets(dlId, resolvedPeriodId);

        Map<String, Object> result = new HashMap<>();
        result.put("evalPeriodId", resolvedPeriodId);
        result.put("targets", targets);
        return result;
    }

    /* periodId null 이면 현재 IN_PROGRESS 기간으로 자동 resolve — 프론트에서 기간 선택 안 해도 동작 */
    private Long resolvePeriodId(Long periodId) {
        if (periodId != null) return periodId;
        Long currentId = mapper.findCurrentPeriodId();
        if (currentId == null) {
            throw new IllegalStateException("현재 진행 중인 평가 기간이 없습니다.");
        }
        return currentId;
    }
}
