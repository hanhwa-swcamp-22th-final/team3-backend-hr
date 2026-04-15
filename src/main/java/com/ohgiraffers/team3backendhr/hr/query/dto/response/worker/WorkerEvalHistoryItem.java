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
    /** 1차(TL) 정성 점수 */
    private Double firstScore;
    /** 2차(DL) 정성 점수 */
    private Double secondScore;
    /** 정량 평가 T-score */
    private Double quantScore;
    /** RECEIVING 상태 이의신청 존재 시 true */
    private Boolean underReview;
    /** 확정 후 7일 이내 & 이의신청 없을 때 true */
    private Boolean appealable;
}
