package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AntiGamingFlagItem {

    private Long flagId;
    private Long employeeId;
    private String employeeName;
    private Integer productionSpeedRank;
    private Integer safetyKeywordRank;
    private Double penaltyCoefficient;
    private Integer targetYear;
    private String targetPeriod;
    private Boolean isActive;
}
