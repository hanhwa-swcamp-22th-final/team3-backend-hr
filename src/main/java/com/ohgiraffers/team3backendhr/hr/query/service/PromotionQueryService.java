package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.worker.WorkerTierHistoryItem;
import com.ohgiraffers.team3backendhr.hr.query.mapper.PromotionQueryMapper;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionQueryService {

    private final PromotionQueryMapper mapper;
    private final AdminClient adminClient;

    /* 승급 심사 요약 */
    public PromotionSummaryResponse getSummary() {
        long total = mapper.countTotal();
        long confirmed = mapper.countConfirmed();
        double rate = total == 0 ? 0.0 : Math.round((double) confirmed / total * 1000.0) / 10.0;
        return new PromotionSummaryResponse(total, confirmed, rate);
    }

    /* 후보 목록 (targetTier 필터, 페이징) */
    public PromotionCandidateListResponse getCandidates(String targetTier, int page, int size) {
        int offset = page * size;
        long totalCount = mapper.countCandidates(targetTier);
        long totalPages = totalCount == 0 ? 0 : (long) Math.ceil((double) totalCount / size);
        return new PromotionCandidateListResponse(
                mapper.findCandidates(targetTier, size, offset), totalCount, totalPages);
    }

    /* 후보 상세 */
    public PromotionCandidateDetailResponse getCandidateDetail(Long candidateId) {
        PromotionCandidateDetailResponse result = mapper.findCandidateById(candidateId);
        if (result == null) {
            throw new BusinessException(ErrorCode.PROMOTION_NOT_FOUND);
        }
        return result;
    }

    /* Worker — 입사 티어를 시작점으로 포함한 본인 티어 성장 이력 */
    public List<WorkerTierHistoryItem> getWorkerTierHistory(Long employeeId) {
        EmployeeProfileResponse profile = adminClient.getWorkerProfile(employeeId);
        List<WorkerTierHistoryItem> promotionHistory = mapper.findWorkerTierHistory(employeeId);

        String currentTier = profile.getCurrentTier() == null ? null : profile.getCurrentTier().name();
        String initialTier = promotionHistory.isEmpty()
                ? currentTier
                : promotionHistory.get(0).getFromTier();

        List<WorkerTierHistoryItem> history = new ArrayList<>();
        history.add(new WorkerTierHistoryItem(
                "INITIAL",
                null,
                initialTier,
                null,
                null,
                "입사 티어",
                profile.getHireDate(),
                null
        ));
        history.addAll(promotionHistory);
        return history;
    }
}
