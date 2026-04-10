package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvalPeriodStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class EvaluationPeriodUpdateRequest {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Long algorithmVersionId;
    private final EvalPeriodStatus status;
}
