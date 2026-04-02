package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

public enum QualEvalStatus {
    NO_INPUT,   // 미입력
    DRAFT,      // 임시저장 중
    SUBMITTED,  // 제출 완료 (level 1·2: TL·DL 제출 후)
    CONFIRMED   // 최종 확정 (level 3: HRM 확정 후)
}
