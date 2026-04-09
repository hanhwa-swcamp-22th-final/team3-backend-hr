package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerQualitativeResponse {

    private Long qualitativeEvaluationId;
    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private String grade;
    private Double score;
    /** JSON 문자열 — {"카테고리": 점수} */
    private String evalItems;
    private String status;
}
