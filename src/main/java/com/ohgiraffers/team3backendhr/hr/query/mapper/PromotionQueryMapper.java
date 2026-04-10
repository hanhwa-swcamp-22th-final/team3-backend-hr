package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PromotionQueryMapper {

    /* 전체 심사 대상 수 */
    long countTotal();

    /* 확정(CONFIRMATION_OF_PROMOTION + TIER_APPLIED) 수 */
    long countConfirmed();

    /* 후보 목록 (targetTier 필터, 페이징) */
    List<PromotionCandidateItem> findCandidates(
            @Param("targetTier") String targetTier,
            @Param("size") int size,
            @Param("offset") int offset);

    /* 후보 수 (페이징용) */
    long countCandidates(String targetTier);

    /* 후보 상세 */
    PromotionCandidateDetailResponse findCandidateById(Long candidateId);
}
