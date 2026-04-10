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
    private Integer evalSequence;
    private String tier;
    private BigDecimal totalScore;
}
