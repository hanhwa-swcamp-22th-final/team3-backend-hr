package com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionCandidateItem {

    private Long tierPromotionId;
    private Long employeeId;
    private String employeeName;
    private String currentTier;
    private String targetTier;
    private Integer tierAccumulatedPoint;
    private Integer targetPromotionPoint;   // 목표 승급 포인트
    private String tierPromoStatus;
    private String createdAt;
}
