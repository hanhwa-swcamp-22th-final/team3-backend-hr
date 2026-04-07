package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TierCriteriaItem {

    private final Long tierConfigId;
    private final String tier;
    private final String tierConfigWeightDistribution;
    private final Integer tierConfigPromotionPoint;
}
