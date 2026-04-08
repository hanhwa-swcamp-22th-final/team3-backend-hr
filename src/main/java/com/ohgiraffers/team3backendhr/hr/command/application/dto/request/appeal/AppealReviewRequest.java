package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.ReviewResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AppealReviewRequest {

    @NotNull(message = "검토 결과는 필수입니다.")
    private final ReviewResult reviewResult;   // ACKNOWLEDGE | ACKNOWLEDGE_IN_PART

    private final Double modifiedScore;        // 점수 변경 시 (null 가능)

    @Size(min = 10, max = 500, message = "사유는 10자 이상 500자 이하여야 합니다.")
    private final String reason;
}
