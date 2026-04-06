package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

public enum MissionType {
    HIGH_DIFFICULTY_WORK,   // matching_record — difficulty_grade IN ('D4','D5')
    KMS_CONTRIBUTION,       // knowledge_article — article_status = 'APPROVED'
    AI_SCORE                // score — capability_index >= condition_value
}
