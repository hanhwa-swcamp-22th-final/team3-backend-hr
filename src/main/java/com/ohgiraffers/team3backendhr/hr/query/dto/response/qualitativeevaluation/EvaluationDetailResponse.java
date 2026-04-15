package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationDetailResponse {

    private Long evalId;
    private Long evaluateeId;
    private String employeeName;
    private String employeeTier;
    private Long evaluatorId;
    private Long evaluationPeriodId;
    private Long evaluationLevel;
    private String evalItems;
    private String evalComment;
    private String grade;
    private Double score;
    private String inputMethod;
    private String status;
    private LocalDateTime createdAt;

    // 1차 평가 (TL)
    private String firstStageComment;
    private Double firstStageScore;

    // 2차 평가 (DL)
    private String secondStageComment;
    private Double secondStageScore;
}
