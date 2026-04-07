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
public class KpiMemberSummaryResponse {

    private Long employeeId;
    private BigDecimal productionScore;
    private BigDecimal eIdxScore;
    private BigDecimal defectRateScore;
}
