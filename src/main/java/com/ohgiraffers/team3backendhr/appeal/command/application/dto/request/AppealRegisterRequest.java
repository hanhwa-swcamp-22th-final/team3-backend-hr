package com.ohgiraffers.team3backendhr.appeal.command.application.dto.request;

import com.ohgiraffers.team3backendhr.appeal.command.domain.aggregate.AppealType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AppealRegisterRequest {

    @NotNull(message = "정성 평가 ID는 필수입니다.")
    private final Long qualitativeEvaluationId;

    @NotNull(message = "이의신청 유형은 필수입니다.")
    private final AppealType appealType;

    @NotNull(message = "제목은 필수입니다.")
    @Size(min = 5, max = 100, message = "제목은 5자 이상 100자 이하여야 합니다.")
    private final String title;

    @NotNull(message = "내용은 필수입니다.")
    @Size(min = 20, max = 2000, message = "내용은 20자 이상 2000자 이하여야 합니다.")
    private final String content;
}
