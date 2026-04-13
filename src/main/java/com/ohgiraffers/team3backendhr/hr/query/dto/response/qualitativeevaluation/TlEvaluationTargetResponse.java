package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TlEvaluationTargetResponse {

    private final Long evalPeriodId;
    private final List<Long> evalPeriodIds;
    private final List<TlEvaluationTargetItem> targets;
    private final Integer evalYear;
    private final Integer evalSequence;
    private final LocalDate startDate;
    private final LocalDate endDate;
}
