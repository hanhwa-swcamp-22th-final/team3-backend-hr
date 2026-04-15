package com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvaluationCategoryWeightItem {

    private final Long evaluationWeightConfigId;
    private final String tierGroup;
    private final String categoryCode;
    private final Integer weightPercent;
}
