package com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HrmTeamStatsItem {

    private Long departmentId;
    private String departmentName;
    private int memberCount;
    private Double avgScore;
    private int sTierCount;
    private int aTierCount;
    private int bTierCount;
    private int cTierCount;
}
