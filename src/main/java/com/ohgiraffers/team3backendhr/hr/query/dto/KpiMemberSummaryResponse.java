package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class KpiMemberSummaryResponse {

    private Long employeeId;
    private String employeeName;
    private BigDecimal uphScore;
    private BigDecimal yieldScore;
    private BigDecimal leadTimeScore;
    private BigDecimal sQuant;
    private BigDecimal tScore;
    private String status;
}
