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
public class ProductionStatsResponse {

    private Long employeeId;
    private Integer year;
    private Integer quarter;
    private BigDecimal targetProduction;
    private BigDecimal actualProduction;
    private BigDecimal defectRate;
    private BigDecimal eIdx;
}
