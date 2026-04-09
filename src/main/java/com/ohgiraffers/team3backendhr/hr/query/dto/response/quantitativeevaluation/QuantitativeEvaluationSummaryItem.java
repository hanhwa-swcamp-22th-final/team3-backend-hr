package com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuantitativeEvaluationSummaryItem {

    private Long quantitativeEvaluationId;
    private Long employeeId;
    private String employeeName;
    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private Double tScore;
    private String status;
}
