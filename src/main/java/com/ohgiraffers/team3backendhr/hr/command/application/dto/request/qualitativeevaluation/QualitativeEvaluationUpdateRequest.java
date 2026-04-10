package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.InputMethod;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualEvalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class QualitativeEvaluationUpdateRequest {

    /** DRAFT(임시저장) | SUBMITTED(제출) */
    @NotNull(message = "status는 필수입니다.")
    private final QualEvalStatus status;

    @NotNull(message = "평가 기간 ID는 필수입니다.")
    private final Long evaluationPeriodId;

    private final String evalItems;

    /** SUBMITTED일 때 서비스 계층에서 필수 검증 (20자 이상) */
    private final String evalComment;

    @NotNull(message = "입력 방식은 필수입니다.")
    private final InputMethod inputMethod;
}
