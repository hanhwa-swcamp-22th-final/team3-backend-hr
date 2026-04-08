package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfileResponse {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String teamName;
    private Grade currentTier;
    private BigDecimal totalScore;
}
