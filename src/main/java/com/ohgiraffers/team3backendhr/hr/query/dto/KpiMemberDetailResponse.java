package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class KpiMemberDetailResponse {

    private Long employeeId;
    private String employeeName;
    private int year;
    private int quarter;
    private Long equipmentId;
    private BigDecimal uphScore;
    private BigDecimal yieldScore;
    private BigDecimal leadTimeScore;
    private BigDecimal actualError;
    private BigDecimal sQuant;
    private BigDecimal tScore;
    private String status;
}
