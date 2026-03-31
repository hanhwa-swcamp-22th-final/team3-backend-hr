package com.ohgiraffers.team3backendhr.hr.command.application.dto.request;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class EvaluationPeriodCreateRequest {

    private final Long algorithmVersionId;
    private final Integer evalYear;
    private final Integer evalSequence;
    private final EvalType evalType;
    private final LocalDate startDate;
    private final LocalDate endDate;
}
