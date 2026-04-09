package com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionCandidateDetailResponse {

    private Long tierPromotionId;
    private Long employeeId;
    private String employeeName;
    private String employeeTier;            // 현재 티어
    private String currentTier;            // 현재 tier_config 티어
    private String targetTier;             // 목표 tier_config 티어
    private Integer tierAccumulatedPoint;
    private Integer targetPromotionPoint;
    private String tierPromoStatus;
    private String tierReviewedAt;
    private String createdAt;
}
