package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerEvalReviewItem {

    private Long qualitativeEvaluationId;
    /** 1 = TL, 2 = DL */
    private Integer evaluationLevel;
    private String grade;
    private Double score;
    /** JSON 문자열 — {"카테고리": 점수} */
    private String evalItems;
    private String evalComment;
    private String status;
    /* 서비스에서 response로 추출 후 사용 — 직접 노출 안 함 */
    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
}
