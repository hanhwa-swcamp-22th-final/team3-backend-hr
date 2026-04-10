package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.AppealStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.ReviewResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AppealStatusUpdateRequest {

    @NotNull(message = "status는 필수입니다.")
    private final AppealStatus status;

    /** COMPLETED일 때 필수: ACKNOWLEDGE | ACKNOWLEDGE_IN_PART | DISMISS */
    private final ReviewResult reviewResult;

    /** 승인 시 점수 변경 (null 가능) */
    private final Double modifiedScore;

    @Size(min = 10, max = 500, message = "사유는 10자 이상 500자 이하여야 합니다.")
    private final String reason;
}
