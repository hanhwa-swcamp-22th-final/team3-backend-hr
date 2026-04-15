package com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DlDashboardTeamItem {

    private Long tlId;
    private String tlName;
    private String teamName;
    private int memberCount;
    private Double teamAvgScore;
    private String evaluationStatus;
}
