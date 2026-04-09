package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.PromotionQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionQueryService {

    private final PromotionQueryMapper mapper;

    /* 승급 심사 요약 */
    public PromotionSummaryResponse getSummary() {
        long total = mapper.countTotal();
        long confirmed = mapper.countConfirmed();
        double rate = total == 0 ? 0.0 : Math.round((double) confirmed / total * 1000.0) / 10.0;
        return new PromotionSummaryResponse(total, confirmed, rate);
    }

    /* 후보 목록 (targetTier 필터, 페이징) */
    public PromotionCandidateListResponse getCandidates(String targetTier, int page, int size) {
        int offset = (page - 1) * size;
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
}
