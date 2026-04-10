package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missionprogress.MissionProgress;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missionprogress.MissionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.MissionTemplate;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.MissionType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.performancepoint.PerformancePoint;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.performancepoint.PointType;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.MissionProgressRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.MissionTemplateRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PerformancePointRepository;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PerformancePointSnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.PromotionEventPublisher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionProgressCommandService {

    private final MissionTemplateRepository missionTemplateRepository;
    private final MissionProgressRepository missionProgressRepository;
    private final PerformancePointRepository performancePointRepository;
    private final IdGenerator idGenerator;
    private final PromotionEventPublisher promotionEventPublisher;

    @Transactional
    public void updateProgress(Long employeeId, MissionType missionType, BigDecimal progressValue, boolean absolute) {
        List<MissionTemplate> templates = missionTemplateRepository.findByMissionTypeAndIsActiveTrue(missionType);

        for (MissionTemplate template : templates) {
            missionProgressRepository
                .findByEmployeeIdAndMissionTemplateId(employeeId, template.getMissionTemplateId())
                .ifPresent(progress -> processProgress(progress, template, progressValue, absolute));
        }
    }

    private void processProgress(
        MissionProgress progress,
        MissionTemplate template,
        BigDecimal progressValue,
        boolean absolute
    ) {
        if (progress.getStatus() == MissionStatus.COMPLETED) {
            return;
        }

        BigDecimal newValue = absolute
            ? progressValue
            : progress.getCurrentValue().add(progressValue);

        boolean wasInProgress = progress.getStatus() == MissionStatus.IN_PROGRESS;
        progress.updateProgress(newValue, template.getConditionValue());

        if (wasInProgress && progress.getStatus() == MissionStatus.COMPLETED) {
            awardPoints(progress.getEmployeeId(), template);
            log.info(
                "[Mission] Completed mission. employeeId={}, template={}, reward={}pt",
                progress.getEmployeeId(),
                template.getMissionName(),
                template.getRewardPoint()
            );
        }

        missionProgressRepository.save(progress);
    }

    private void awardPoints(Long employeeId, MissionTemplate template) {
        PointType pointType = resolvePointType(template.getMissionType());
        PerformancePoint point = PerformancePoint.builder()
            .performancePointId(idGenerator.generate())
            .performanceEmployeeId(employeeId)
            .pointType(pointType)
            .pointAmount(BigDecimal.valueOf(template.getRewardPoint()))
            .pointEarnedDate(LocalDate.now())
            .pointSourceId(template.getMissionTemplateId())
            .pointSourceType("MISSION")
            .pointDescription(template.getMissionName() + " mission completion reward")
            .build();

        PerformancePoint saved = performancePointRepository.save(point);
        publishSnapshotAfterCommit(saved);
    }

    private void publishSnapshotAfterCommit(PerformancePoint performancePoint) {
        PerformancePointSnapshotEvent snapshotEvent = PerformancePointSnapshotEvent.builder()
            .performancePointId(performancePoint.getPerformancePointId())
            .employeeId(performancePoint.getPerformanceEmployeeId())
            .pointType(performancePoint.getPointType() == null ? null : performancePoint.getPointType().name())
            .pointAmount(performancePoint.getPointAmount())
            .pointEarnedDate(performancePoint.getPointEarnedDate())
            .pointSourceId(performancePoint.getPointSourceId())
            .pointSourceType(performancePoint.getPointSourceType())
            .pointDescription(performancePoint.getPointDescription())
            .occurredAt(LocalDateTime.now())
            .build();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    promotionEventPublisher.publishPerformancePointSnapshot(snapshotEvent);
                }
            });
            return;
        }

        promotionEventPublisher.publishPerformancePointSnapshot(snapshotEvent);
    }

    private PointType resolvePointType(MissionType missionType) {
        return switch (missionType) {
            case HIGH_DIFFICULTY_WORK -> PointType.CHALLENGE;
            case KMS_CONTRIBUTION -> PointType.KNOWLEDGE_SHARING;
            case AI_SCORE -> PointType.QUALITATIVE;
        };
    }
}