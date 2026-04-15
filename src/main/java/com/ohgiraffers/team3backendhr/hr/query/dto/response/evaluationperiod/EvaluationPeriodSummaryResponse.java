package com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationperiod;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationPeriodSummaryResponse {

    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long algorithmVersionId;
}
