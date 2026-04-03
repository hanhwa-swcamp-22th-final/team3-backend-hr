package com.ohgiraffers.team3backendhr.hr.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EvaluationPeriodListResponse {

    private final List<EvaluationPeriodSummaryResponse> content;
    private final long totalElements;
    private final long totalPages;
}
