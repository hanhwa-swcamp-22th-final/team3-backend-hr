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
    /** level 3(HRM) 정성 평가 상태 — 없으면 NO_INPUT */
    private String qualStatus;
    /** 같은 팀 내 최신 포인트 순위 */
    private Integer rank;
    /** 같은 팀 내 최신 포인트 순위 집계 대상 인원 수 */
    private Integer rankTotal;
}
