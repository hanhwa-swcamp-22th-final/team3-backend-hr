package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionResponse {

    private Long missionProgressId;
    private Long missionTemplateId;
    private String missionName;
    private String missionType;
    private String upgradeToTier;
    private BigDecimal currentValue;
    private BigDecimal conditionValue;
    private Integer progressRate;
    private String status;
    private Integer rewardPoint;
    private LocalDateTime completedAt;
}
