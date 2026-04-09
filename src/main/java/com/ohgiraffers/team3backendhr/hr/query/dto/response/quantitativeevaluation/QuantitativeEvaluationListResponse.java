package com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuantitativeEvaluationListResponse {

    private final List<QuantitativeEvaluationSummaryItem> content;
    private final long totalCount;
    private final long totalPages;
}
