package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WorkerEvalReviewResponse {

    private final Long evalPeriodId;
    private final Integer evalYear;
    private final Integer evalSequence;
    /** level=1 (TL) 결과 — SUBMITTED 상태일 때만 존재 */
    private final WorkerEvalReviewItem firstEval;
    /** level=2 (DL) 결과 — SUBMITTED 상태일 때만 존재 */
    private final WorkerEvalReviewItem secondEval;
    /** firstEval·secondEval 모두 존재할 때 true */
    private final boolean appealable;
}
