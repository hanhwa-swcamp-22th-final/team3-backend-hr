package com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HrmKpiTrendItem {

    private int year;
    private int evalSequence;
    private Double avgScore;
    private int tierSCount;
    private int tierACount;
    private int tierBCount;
    private int tierCCount;
}
