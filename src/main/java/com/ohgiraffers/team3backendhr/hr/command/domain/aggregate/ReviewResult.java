package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

public enum ReviewResult {
    ACKNOWLEDGE,          // 인용
    ACKNOWLEDGE_IN_PART,  // 일부 인용
    DISMISS               // 기각
}
