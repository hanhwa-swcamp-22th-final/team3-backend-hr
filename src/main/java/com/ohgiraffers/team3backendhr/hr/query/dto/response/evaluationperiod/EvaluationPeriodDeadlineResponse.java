package com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationperiod;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationPeriodDeadlineResponse {

    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private LocalDate endDate;
    private Long daysRemaining;
    private String status;
}
