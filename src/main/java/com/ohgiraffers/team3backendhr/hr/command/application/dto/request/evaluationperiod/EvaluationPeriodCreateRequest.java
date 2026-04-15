package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class EvaluationPeriodCreateRequest {

    @NotNull(message = "알고리즘 버전은 필수입니다.")
    private final Long algorithmVersionId;

    @NotNull(message = "평가 연도는 필수입니다.")
    private final Integer evalYear;

    @NotNull(message = "평가 차수는 필수입니다.")
    private final Integer evalSequence;

    @NotNull(message = "시작일은 필수입니다.")
    private final LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private final LocalDate endDate;
}
