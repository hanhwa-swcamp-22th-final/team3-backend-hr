package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PromotionHistoryRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PromotionHistorySnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.PromotionEventPublisher;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionCommandService {

    private final PromotionHistoryRepository promotionHistoryRepository;
    private final TierConfigRepository tierConfigRepository;
    private final AdminClient adminClient;
    private final PromotionEventPublisher promotionEventPublisher;

    public void confirmPromotion(Long tierPromotionId, Long reviewerId) {
        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROMOTION_NOT_FOUND));
        history.confirm(reviewerId);
        publishSnapshotsAfterCommit(List.of(toSnapshotEvent(history)));
    }

    public void applyTierForConfirmed() {
        List<PromotionHistorySnapshotEvent> snapshotEvents = new ArrayList<>();

        promotionHistoryRepository.findByTierPromoStatus(PromotionStatus.CONFIRMATION_OF_PROMOTION)
            .forEach(history -> {
                TierConfig targetTierConfig = tierConfigRepository.findById(history.getTargetTierConfigId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TIER_CONFIG_NOT_FOUND));
                Grade newTier = targetTierConfig.getTierConfigTier();
                adminClient.updateEmployeeTier(history.getEmployeeId(), newTier);
                history.applyTier();
                snapshotEvents.add(toSnapshotEvent(history));
            });

        publishSnapshotsAfterCommit(snapshotEvents);
    }

    public void suspendPromotion(Long tierPromotionId, Long reviewerId) {
        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROMOTION_NOT_FOUND));
        history.suspend(reviewerId);
        publishSnapshotsAfterCommit(List.of(toSnapshotEvent(history)));
    }

    private PromotionHistorySnapshotEvent toSnapshotEvent(PromotionHistory history) {
        return PromotionHistorySnapshotEvent.builder()
            .tierPromotionId(history.getTierPromotionId())
            .employeeId(history.getEmployeeId())
            .reviewerId(history.getReviewerId())
            .currentTierConfigId(history.getCurrentTierConfigId())
            .targetTierConfigId(history.getTargetTierConfigId())
            .tierConfigEffectiveDate(history.getTierConfigEffectiveDate())
            .tierAccumulatedPoint(history.getTierAccumulatedPoint())
            .promotionStatus(history.getTierPromoStatus() == null ? null : history.getTierPromoStatus().name())
            .tierReviewedAt(history.getTierReviewedAt())
            .occurredAt(LocalDateTime.now())
            .build();
    }

    private void publishSnapshotsAfterCommit(List<PromotionHistorySnapshotEvent> snapshotEvents) {
        if (snapshotEvents.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    snapshotEvents.forEach(promotionEventPublisher::publishPromotionHistorySnapshot);
                }
            });
            return;
        }

        snapshotEvents.forEach(promotionEventPublisher::publishPromotionHistorySnapshot);
    }
}
