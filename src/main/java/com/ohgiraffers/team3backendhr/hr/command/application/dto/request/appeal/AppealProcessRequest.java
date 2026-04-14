package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AppealProcessRequest {

    private final Double modifiedScore;

    @Size(min = 10, max = 500, message = "사유는 10자 이상 500자 이하여야 합니다.")
    private final String reason;
}
