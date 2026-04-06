package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointSummaryResponse {

    private BigDecimal totalPoints;
}
