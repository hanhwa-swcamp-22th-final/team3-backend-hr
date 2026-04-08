package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BiasReportItem {

    private Long biasCorrectionId;
    private Long evaluatorId;
    private String evaluatorName;
    private Long qualitativeEvaluationId;
    private String biasType;
    private Double evaluatorAvg;
    private Double companyAvg;
    private Double alphaBias;
    private Double originalScore;
    private Double correctedScore;
    private LocalDateTime detectedAt;
}
