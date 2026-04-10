package com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionCandidateDetailResponse {

    private Long tierPromotionId;
    private Long employeeId;
    private String employeeName;
    /** employee.employee_tier — apply-tier 완료 후 실제 반영된 티어 (confirm 직후에는 갱신 전일 수 있음) */
    private String employeeTier;
    /** current_tier_config_id 기준 — 승급 심사 등록 시점의 티어 */
    private String currentTier;
    private String targetTier;             // 목표 tier_config 티어
    private Integer tierAccumulatedPoint;
    private Integer targetPromotionPoint;
    private String tierPromoStatus;
    private String tierReviewedAt;
    private String createdAt;
}
