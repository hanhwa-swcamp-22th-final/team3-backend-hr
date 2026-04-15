package com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TlDashboardSummaryResponse {

    private Double teamAvgScore;
    private Double evaluationRate;
    private int completedCount;
    private int tierSCount;
    private int tierACount;
    private int tierBCount;
    private int tierCCount;
}
