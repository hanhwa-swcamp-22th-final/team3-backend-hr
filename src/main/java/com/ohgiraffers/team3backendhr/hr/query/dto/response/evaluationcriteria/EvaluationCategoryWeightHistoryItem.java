package com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvaluationCategoryWeightHistoryItem {

    private final Long evaluationWeightConfigId;
    private final String tierGroup;
    private final String categoryCode;
    private final Integer weightPercent;
    private final Boolean active;
    private final Boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
