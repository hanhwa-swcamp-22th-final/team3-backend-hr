package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiMemberDetailResponse {

    private Long employeeId;
    private Integer year;
    private Integer quarter;
    private BigDecimal targetProduction;
    private BigDecimal actualProduction;
    private BigDecimal defectRate;
    private BigDecimal eIdx;
    private BigDecimal productionScore;
    private BigDecimal eIdxScore;
    private BigDecimal defectRateScore;
}
