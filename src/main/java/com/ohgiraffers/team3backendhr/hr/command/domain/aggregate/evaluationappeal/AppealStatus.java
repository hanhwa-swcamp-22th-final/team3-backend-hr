package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal;

public enum AppealStatus {
    RECEIVING,   // 접수
    REVIEWING,   // 보류(검토 중)
    COMPLETED    // 승인·반려 완료
}
