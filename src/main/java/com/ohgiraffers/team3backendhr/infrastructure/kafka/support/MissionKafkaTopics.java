package com.ohgiraffers.team3backendhr.infrastructure.kafka.support;

public final class MissionKafkaTopics {

    /**
     * SCM·KMS·AI 배치가 미션 진행값을 HR로 전달하는 토픽.
     * payload: { employeeId, missionType, progressValue, absolute }
     */
    public static final String MISSION_PROGRESS_UPDATED = "hr.mission.progress-updated";

    private MissionKafkaTopics() {
    }
}
