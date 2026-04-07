package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationSummaryItem {

    private Long evalId;
    private Long evaluateeId;
    private String employeeName;
    private String employeeTier;
    private Long evaluationPeriodId;
    private Long evaluationLevel;   // 1=TL, 2=DL, 3=HRM
    private String grade;           // S | A | B | C | null
    private Double score;
    private String status;          // NO_INPUT | DRAFT | SUBMITTED | CONFIRMED
}
