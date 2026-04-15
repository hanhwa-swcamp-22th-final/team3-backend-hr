package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerGrowthTrendItem {

    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private Double myScore;
    private Double teamAverageScore;
    private Double companyAverageScore;
}
