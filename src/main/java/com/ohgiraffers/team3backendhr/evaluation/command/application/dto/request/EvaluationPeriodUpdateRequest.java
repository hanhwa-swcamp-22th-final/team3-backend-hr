package com.ohgiraffers.team3backendhr.evaluation.command.application.dto.request;

import java.time.LocalDate;

public record EvaluationPeriodUpdateRequest(
        LocalDate startDate,
        LocalDate endDate,
        Long algorithmVersionId
) {}
