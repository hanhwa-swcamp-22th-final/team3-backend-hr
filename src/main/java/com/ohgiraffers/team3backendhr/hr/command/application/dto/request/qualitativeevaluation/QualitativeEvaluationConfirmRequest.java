package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.InputMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class QualitativeEvaluationConfirmRequest {

    @NotNull(message = "평가 기간 ID는 필수입니다.")
    private final Long evaluationPeriodId;

    private final String evalComment;

    @NotNull(message = "입력 방식은 필수입니다.")
    private final InputMethod inputMethod;
}
