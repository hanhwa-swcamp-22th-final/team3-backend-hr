package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notification.NotificationType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PromotionHistoryRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PromotionCandidateEvaluatedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PromotionHistorySnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.PromotionEventPublisher;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionCandidateCommandService {

    private final PromotionHistoryRepository promotionHistoryRepository;
    private final IdGenerator idGenerator;
    private final PromotionEventPublisher promotionEventPublisher;
    private final QualitativeEvaluationRepository qualitativeEvaluationRepository;
    private final NotificationCommandService notificationCommandService;

    public void applyCandidate(PromotionCandidateEvaluatedEvent event) {
        PromotionStatus requestedStatus = event.getPromotionStatus() == null
            ? PromotionStatus.UNDER_REVIEW
            : PromotionStatus.valueOf(event.getPromotionStatus());

        PromotionHistory promotionHistory = promotionHistoryRepository
            .findFirstByEmployeeIdAndCurrentTierConfigIdAndTargetTierConfigIdAndTierPromoStatusIn(
                event.getEmployeeId(),
                event.getCurrentTierConfigId(),
                event.getTargetTierConfigId(),
                EnumSet.of(PromotionStatus.UNDER_REVIEW, PromotionStatus.CONFIRMATION_OF_PROMOTION)
            )
            .orElseGet(() -> PromotionHistory.builder()
                .tierPromotionId(idGenerator.generate())
                .employeeId(event.getEmployeeId())
                .currentTierConfigId(event.getCurrentTierConfigId())
                .targetTierConfigId(event.getTargetTierConfigId())
                .tierPromoStatus(requestedStatus)
                .build());

        promotionHistory.syncFromBatch(
            event.getCurrentTierConfigId(),
            event.getTargetTierConfigId(),
            event.getTierConfigEffectiveDate(),
            event.getTierAccumulatedPoint(),
            requestedStatus
        );

        PromotionHistory saved = promotionHistoryRepository.save(promotionHistory);
        sendPromotionNotification(event);
        publishSnapshotAfterCommit(saved, event.getOccurredAt());
    }

    private void sendPromotionNotification(PromotionCandidateEvaluatedEvent event) {
        List<Long> recipients = new ArrayList<>();
        qualitativeEvaluationRepository
            .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                event.getEmployeeId(), event.getEvaluationPeriodId(), 1L)
            .map(QualitativeEvaluation::getEvaluatorId)
            .ifPresent(recipients::add);
        qualitativeEvaluationRepository
            .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                event.getEmployeeId(), event.getEvaluationPeriodId(), 2L)
            .map(QualitativeEvaluation::getEvaluatorId)
            .ifPresent(recipients::add);

        if (!recipients.isEmpty()) {
            notificationCommandService.create(
                NotificationType.PROMOTION,
                "승급 후보 확정",
                event.getEmployeeId() + "번 직원이 " + event.getCurrentTier() + " → " + event.getTargetTier() + " 승급 후보로 선정되었습니다.",
                recipients
            );
        }
    }

    private void publishSnapshotAfterCommit(PromotionHistory promotionHistory, LocalDateTime occurredAt) {
        PromotionHistorySnapshotEvent snapshotEvent = PromotionHistorySnapshotEvent.builder()
            .tierPromotionId(promotionHistory.getTierPromotionId())
            .employeeId(promotionHistory.getEmployeeId())
            .reviewerId(promotionHistory.getReviewerId())
            .currentTierConfigId(promotionHistory.getCurrentTierConfigId())
            .targetTierConfigId(promotionHistory.getTargetTierConfigId())
            .tierConfigEffectiveDate(promotionHistory.getTierConfigEffectiveDate())
            .tierAccumulatedPoint(promotionHistory.getTierAccumulatedPoint())
            .promotionStatus(promotionHistory.getTierPromoStatus() == null ? null : promotionHistory.getTierPromoStatus().name())
            .tierReviewedAt(promotionHistory.getTierReviewedAt())
            .occurredAt(occurredAt == null ? LocalDateTime.now() : occurredAt)
            .build();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    promotionEventPublisher.publishPromotionHistorySnapshot(snapshotEvent);
                }
            });
            return;
        }

        promotionEventPublisher.publishPromotionHistorySnapshot(snapshotEvent);
    }
}
