package com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HrmKpiTrendItem {

    private int year;
    private int quarter;
    private Double avgScore;
    private int sTierCount;
    private int aTierCount;
    private int bTierCount;
    private int cTierCount;
}
