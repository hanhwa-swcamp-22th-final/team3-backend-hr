package com.ohgiraffers.team3backendhr.appeal.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AppealSummaryResponse {

    private Long appealId;
    private Long qualitativeEvaluationId;
    private Long appealEmployeeId;
    private String employeeName;
    private String appealType;
    private String title;
    private String status;
    private String reviewResult;
    private LocalDateTime filedAt;
    private LocalDateTime reviewedAt;
}
