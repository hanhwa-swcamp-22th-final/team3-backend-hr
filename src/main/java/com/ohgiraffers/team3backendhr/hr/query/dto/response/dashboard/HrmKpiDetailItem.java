package com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrmKpiDetailItem {

    private Long employeeId;
    private String employeeName;
    private String employeeTier;
    private String departmentName;
    private Double qualitativeScore;
    private String grade;
    private String evalStatus;

    public HrmKpiDetailItem(Long employeeId, String employeeName, String employeeTier,
                            Double qualitativeScore, String grade, String evalStatus) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeTier = employeeTier;
        this.qualitativeScore = qualitativeScore;
        this.grade = grade;
        this.evalStatus = evalStatus;
    }
}
