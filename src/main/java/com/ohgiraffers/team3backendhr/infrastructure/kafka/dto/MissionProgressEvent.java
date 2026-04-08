package com.ohgiraffers.team3backendhr.infrastructure.kafka.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 미션 진행값 갱신 이벤트.
 *
 * <p>SCM, KMS, AI 배치 등 외부 서비스에서 발행하며 HR이 수신한다.</p>
 *
 * <ul>
 *   <li>HIGH_DIFFICULTY_WORK — SCM이 D4·D5 난이도 작업 완료 시 progressValue=1 로 발행</li>
 *   <li>KMS_CONTRIBUTION    — KMS가 지식 문서 승인 시 progressValue=1 로 발행</li>
 *   <li>AI_SCORE            — AI 배치가 역량 지수 계산 후 절댓값(absolute=true)으로 발행</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
public class MissionProgressEvent {

    private Long employeeId;

    /** MissionType enum 명칭 문자열 (HIGH_DIFFICULTY_WORK | KMS_CONTRIBUTION | AI_SCORE) */
    private String missionType;

    /** 증가량(count 기반) 또는 절댓값(AI_SCORE) */
    private BigDecimal progressValue;

    /**
     * true  → progressValue 를 current_value 로 덮어씀 (AI_SCORE)
     * false → current_value += progressValue (카운트 기반)
     */
    private boolean absolute;
}
