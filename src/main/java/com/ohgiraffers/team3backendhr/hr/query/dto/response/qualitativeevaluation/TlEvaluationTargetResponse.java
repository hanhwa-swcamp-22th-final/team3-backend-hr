package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TlEvaluationTargetResponse {

    private final Long evalPeriodId;
    private final List<Long> evalPeriodIds;
    private final List<TlEvaluationTargetItem> targets;
}
