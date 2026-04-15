package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal;

public enum AppealStatus {
    SUBMITTED,   // 사원 제출
    RECEIVING,   // HRM 접수, TL 처리 대기
    REVIEWING,   // TL 처리 후 DL 검토 단계
    COMPLETED    // 최종 종료
}
