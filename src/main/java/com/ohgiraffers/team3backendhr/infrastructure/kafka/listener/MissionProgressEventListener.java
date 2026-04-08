package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendhr.hr.command.application.service.MissionProgressCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.MissionType;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.MissionProgressEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.MissionKafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionProgressEventListener {

    private final MissionProgressCommandService missionProgressCommandService;

    @KafkaListener(
            topics = MissionKafkaTopics.MISSION_PROGRESS_UPDATED,
            containerFactory = "missionProgressKafkaListenerContainerFactory"
    )
    public void listen(MissionProgressEvent event) {
        log.info("[Mission] 이벤트 수신 — employeeId={}, type={}, value={}, absolute={}",
                event.getEmployeeId(), event.getMissionType(),
                event.getProgressValue(), event.isAbsolute());

        MissionType missionType;
        try {
            missionType = MissionType.valueOf(event.getMissionType());
        } catch (IllegalArgumentException e) {
            log.warn("[Mission] 알 수 없는 missionType: {}", event.getMissionType());
            return;
        }

        missionProgressCommandService.updateProgress(
                event.getEmployeeId(),
                missionType,
                event.getProgressValue(),
                event.isAbsolute()
        );
    }
}
