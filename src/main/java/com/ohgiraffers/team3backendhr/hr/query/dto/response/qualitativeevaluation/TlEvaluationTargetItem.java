package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TlEvaluationTargetItem {

    private Long evaluationPeriodId;
    private Long evaluateeId;
    private String employeeCode;
    private String employeeName;
    private String employeeTier;
    private Long evalId;
    private String status;
    private Boolean submitted;
    private Integer totalScore;
    private String evalComment;
}
