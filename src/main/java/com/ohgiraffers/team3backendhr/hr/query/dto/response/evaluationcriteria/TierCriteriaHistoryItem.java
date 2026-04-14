package com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TierCriteriaHistoryItem {

    private final Long tierConfigId;
    private final String tier;
    private final Integer tierConfigPromotionPoint;
    private final Boolean active;
    private final Boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
