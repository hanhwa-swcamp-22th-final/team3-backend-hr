package com.ohgiraffers.team3backendhr.hr.command.application.dto.request;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.InputMethod;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class QualitativeEvaluationSubmitRequest {

    @NotNull(message = "평가 기간 ID는 필수입니다.")
    private final Long evaluationPeriodId;

    private final String evalItems;

    @NotNull(message = "평가 코멘트는 필수입니다.")
    @Size(min = 20, max = 2000, message = "평가 코멘트는 20자 이상 2000자 이하이어야 합니다.")
    private final String evalComment;

    @NotNull(message = "입력 방식은 필수입니다.")
    private final InputMethod inputMethod;

    @NotNull(message = "점수는 필수입니다.")
    @DecimalMin(value = "0.0", message = "점수는 0 이상이어야 합니다.")
    @DecimalMax(value = "100.0", message = "점수는 100 이하이어야 합니다.")
    private final Double score;
}
