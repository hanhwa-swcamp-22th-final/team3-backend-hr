package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DlEvaluationDetailResponse {

    private Long evalId;
    private Long evaluateeId;
    private String employeeName;
    private String employeeTier;
    private String evaluatorName;       // TL 이름
    private Long evaluationPeriodId;
    private String evalItems;           // 1차 평가 항목 JSON
    private String evalComment;         // 1차 평가 코멘트
    private Double firstStageScore;     // 1차 score (batch 분석 후 세팅)
    private String firstStageGrade;     // 1차 grade
    private Double aiRecommendedScore;  // AI 추천 점수 (현재 null)
    private String status;
}
