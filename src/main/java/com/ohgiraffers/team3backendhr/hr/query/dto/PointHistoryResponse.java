package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryResponse {

    private Long performancePointId;
    private String pointType;
    private BigDecimal pointAmount;
    private LocalDate pointEarnedDate;
    private String pointDescription;
    private String pointSourceType;
}
