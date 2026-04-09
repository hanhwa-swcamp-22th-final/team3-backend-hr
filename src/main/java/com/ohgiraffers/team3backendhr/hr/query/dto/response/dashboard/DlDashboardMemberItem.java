package com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DlDashboardMemberItem {

    private Long employeeId;
    private String employeeName;
    private String employeeTier;
    private Double qualitativeScore;
    private String grade;
    private String evalStatus;
}
