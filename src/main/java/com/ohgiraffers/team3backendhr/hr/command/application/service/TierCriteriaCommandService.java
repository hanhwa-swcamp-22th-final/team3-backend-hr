package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.TierCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.TierConfigSnapshotEvent;
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
public class TierCriteriaCommandService {

    private final TierConfigRepository tierConfigRepository;
    private final IdGenerator idGenerator;
    private final PromotionEventPublisher promotionEventPublisher;

    public void saveCriteria(List<TierCriteriaSaveRequest> requests) {
        List<TierConfigSnapshotEvent> snapshotEvents = new ArrayList<>();

        for (TierCriteriaSaveRequest req : requests) {
            TierConfig config = TierConfig.builder()
                .tierConfigId(idGenerator.generate())
                .tierConfigTier(Grade.valueOf(req.getTier()))
                .tierConfigPromotionPoint(req.getTierConfigPromotionPoint())
                .equipmentResponseTargetScore(req.getEquipmentResponseTargetScore())
                .technicalTransferTargetScore(req.getTechnicalTransferTargetScore())
                .innovationProposalTargetScore(req.getInnovationProposalTargetScore())
                .safetyComplianceTargetScore(req.getSafetyComplianceTargetScore())
                .qualityManagementTargetScore(req.getQualityManagementTargetScore())
                .productivityTargetScore(req.getProductivityTargetScore())
                .build();
            TierConfig saved = tierConfigRepository.save(config);

            snapshotEvents.add(TierConfigSnapshotEvent.builder()
                .tierConfigId(saved.getTierConfigId())
                .tier(saved.getTierConfigTier().name())
                .weightDistribution(null)
                .promotionPoint(saved.getTierConfigPromotionPoint())
                .occurredAt(LocalDateTime.now())
                .build());
        }

        publishSnapshotsAfterCommit(snapshotEvents);
    }

    private void publishSnapshotsAfterCommit(List<TierConfigSnapshotEvent> snapshotEvents) {
        if (snapshotEvents.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    snapshotEvents.forEach(promotionEventPublisher::publishTierConfigSnapshot);
                }
            });
            return;
        }

        snapshotEvents.forEach(promotionEventPublisher::publishTierConfigSnapshot);
    }
}
