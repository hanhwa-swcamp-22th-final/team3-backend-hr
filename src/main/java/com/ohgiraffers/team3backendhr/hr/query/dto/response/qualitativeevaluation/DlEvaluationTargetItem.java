package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DlEvaluationTargetItem {

    private Long evaluateeId;
    private String employeeName;
    private String employeeTier;
    private Long evalId;
    private Double firstStageScore;
    private Double aiRecommendedScore;  // 현재 null 반환 (AI 미구현)
    private String status;
}
