package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierMilestoneResponse {

    private Long milestoneId;
    private String description;
    private Integer requiredPoint;
    private boolean isAchieved;
}
