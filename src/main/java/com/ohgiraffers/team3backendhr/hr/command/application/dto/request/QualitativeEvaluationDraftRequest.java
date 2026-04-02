package com.ohgiraffers.team3backendhr.hr.command.application.dto.request;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.InputMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class QualitativeEvaluationDraftRequest {

    @NotNull(message = "평가 기간 ID는 필수입니다.")
    private final Long evaluationPeriodId;

    private final String evalItems;    // 임시저장 시 null 허용

    private final String evalComment;  // 임시저장 시 null 허용

    @NotNull(message = "입력 방식은 필수입니다.")
    private final InputMethod inputMethod;
}
