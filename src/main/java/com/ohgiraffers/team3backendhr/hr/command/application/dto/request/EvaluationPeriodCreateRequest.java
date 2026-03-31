package com.ohgiraffers.team3backendhr.hr.command.application.dto.request;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalType;

import java.time.LocalDate;

public record EvaluationPeriodCreateRequest(
        Long algorithmVersionId,
        Integer evalYear,
        Integer evalSequence,
        EvalType evalType,
        LocalDate startDate,
        LocalDate endDate
) {}
