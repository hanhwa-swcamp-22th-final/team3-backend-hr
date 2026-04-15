package com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AppealSummaryResponse {

    private Long appealId;
    private Long appealEmployeeId;
    private Long evaluationPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private String employeeName;
    private String employeeCode;
    private String employeeTier;
    private String teamName;
    private String departmentName;
    private String appealType;
    private String title;
    private String content;
    private String status;
    private String reviewResult;
    private Double firstScore;
    private Double secondScore;
    private Double finalScore;
    private LocalDateTime filedAt;
    private LocalDateTime reviewedAt;
}
