package com.ohgiraffers.team3backendhr.hr.query.dto.response.tierconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TierCriteriaItem {

    private final Long tierConfigId;
    private final String tier;
    private final Integer tierConfigPromotionPoint;
}
