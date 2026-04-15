package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class WorkerEvalHistoryItem {

    private Long qualitativeEvaluationId;
    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private String grade;
    private Double score;
    private String status;
    private LocalDateTime confirmedAt;
    /** 확정 후 7일 이내이면 true */
    private Boolean appealable;
}
