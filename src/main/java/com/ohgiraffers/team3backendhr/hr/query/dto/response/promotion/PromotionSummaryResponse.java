package com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PromotionSummaryResponse {

    private final long totalCandidates;     // 전체 심사 대상 수
    private final long confirmedCount;      // 확정 수
    private final double promotionRate;     // 승급률 (%)
}
