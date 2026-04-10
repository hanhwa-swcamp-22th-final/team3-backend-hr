package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.performancepoint.PerformancePoint;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.performancepoint.PointType;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PerformancePointRepository;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PerformancePointSnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.PromotionEventPublisher;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class PerformancePointCommandService {

    private final PerformancePointRepository performancePointRepository;
    private final IdGenerator idGenerator;
    private final PromotionEventPublisher promotionEventPublisher;

    public void applyCalculatedPoint(PerformancePointCalculatedEvent event) {
        PointType pointType = PointType.valueOf(event.getPointType());
        PerformancePoint performancePoint = performancePointRepository
            .findByPerformanceEmployeeIdAndPointTypeAndPointSourceIdAndPointSourceType(
                event.getEmployeeId(),
                pointType,
                event.getPointSourceId(),
                event.getPointSourceType()
            )
            .orElseGet(() -> PerformancePoint.builder()
                .performancePointId(idGenerator.generate())
                .performanceEmployeeId(event.getEmployeeId())
                .build());

        performancePoint.applyCalculated(
            pointType,
            event.getPointAmount(),
            event.getPointEarnedDate(),
            event.getPointSourceId(),
            event.getPointSourceType(),
            event.getPointDescription()
        );

        PerformancePoint saved = performancePointRepository.save(performancePoint);
        publishSnapshotAfterCommit(saved, event.getOccurredAt());
    }

    private void publishSnapshotAfterCommit(PerformancePoint performancePoint, LocalDateTime occurredAt) {
        PerformancePointSnapshotEvent snapshotEvent = PerformancePointSnapshotEvent.builder()
            .performancePointId(performancePoint.getPerformancePointId())
            .employeeId(performancePoint.getPerformanceEmployeeId())
            .pointType(performancePoint.getPointType() == null ? null : performancePoint.getPointType().name())
            .pointAmount(performancePoint.getPointAmount())
            .pointEarnedDate(performancePoint.getPointEarnedDate())
            .pointSourceId(performancePoint.getPointSourceId())
            .pointSourceType(performancePoint.getPointSourceType())
            .pointDescription(performancePoint.getPointDescription())
            .occurredAt(occurredAt == null ? LocalDateTime.now() : occurredAt)
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
}
