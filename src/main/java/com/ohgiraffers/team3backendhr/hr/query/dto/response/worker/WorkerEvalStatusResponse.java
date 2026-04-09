package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerEvalStatusResponse {

    private Long evalPeriodId;
    private Integer evalYear;
    private Integer evalSequence;
    private String evalType;
    /** level 3(HRM) 정성 평가 상태 — 없으면 NO_INPUT */
    private String qualStatus;
}
