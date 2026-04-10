package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierChartPointResponse {

    private Integer year;
    private String evaluationPeriod;  // Admin score.evaluation_period (예: "Q1", "1분기")
    private String tier;
    private BigDecimal totalScore;    // Admin score.total_points에서 변환
}
