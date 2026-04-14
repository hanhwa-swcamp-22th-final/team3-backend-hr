package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.EvaluationCategoryWeightSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.EvaluationCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.TierCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcriteria.EvaluationCategory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcriteria.EvaluationTierGroup;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcriteria.EvaluationWeightConfig;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.EvaluationWeightConfigSnapshotEvent;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationWeightConfigRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.TierConfigSnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.PromotionEventPublisher;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    private final EvaluationWeightConfigRepository evaluationWeightConfigRepository;
    private final IdGenerator idGenerator;
    private final PromotionEventPublisher promotionEventPublisher;

    public void createCriteria(EvaluationCriteriaSaveRequest request) {
        persistCriteria(request);
    }

    public void updateCriteria(EvaluationCriteriaSaveRequest request) {
        persistCriteria(request);
    }

    public void deleteCriteria() {
        List<TierConfigSnapshotEvent> snapshotEvents = new ArrayList<>();
        List<EvaluationWeightConfigSnapshotEvent> weightSnapshotEvents = new ArrayList<>();

        for (Grade grade : Grade.values()) {
            tierConfigRepository.findByTierConfigTierAndActiveTrueAndDeletedFalse(grade)
                .ifPresent(existing -> {
                    existing.softDelete();
                    TierConfig saved = tierConfigRepository.save(existing);
                    snapshotEvents.add(buildSnapshotEvent(saved));
                });
        }

        for (EvaluationTierGroup tierGroup : EvaluationTierGroup.values()) {
            for (EvaluationCategory category : EvaluationCategory.values()) {
                evaluationWeightConfigRepository
                    .findByTierGroupAndCategoryCodeAndActiveTrueAndDeletedFalse(
                        tierGroup,
                        category
                    )
                    .ifPresent(existing -> {
                        existing.softDelete();
                        EvaluationWeightConfig saved = evaluationWeightConfigRepository.save(existing);
                        weightSnapshotEvents.add(buildWeightSnapshotEvent(saved));
                    });
            }
        }

        publishSnapshotsAfterCommit(snapshotEvents, weightSnapshotEvents);
    }

    public void saveCriteria(EvaluationCriteriaSaveRequest request) {
        persistCriteria(request);
    }

    private void persistCriteria(EvaluationCriteriaSaveRequest request) {
        validateWeightTotals(request.getCategoryWeights());

        List<TierConfigSnapshotEvent> snapshotEvents = new ArrayList<>();
        List<EvaluationWeightConfigSnapshotEvent> weightSnapshotEvents = new ArrayList<>();

        for (TierCriteriaSaveRequest req : request.getTierConfigs()) {
            Grade tier = Grade.valueOf(req.getTier());

            tierConfigRepository.findByTierConfigTierAndActiveTrueAndDeletedFalse(tier)
                .ifPresent(existing -> {
                    existing.deactivate();
                    TierConfig deactivated = tierConfigRepository.save(existing);
                    snapshotEvents.add(buildSnapshotEvent(deactivated));
                });

            TierConfig config = TierConfig.builder()
                .tierConfigId(idGenerator.generate())
                .tierConfigTier(tier)
                .tierConfigPromotionPoint(req.getTierConfigPromotionPoint())
                .active(Boolean.TRUE)
                .deleted(Boolean.FALSE)
                .build();
            TierConfig saved = tierConfigRepository.save(config);

            snapshotEvents.add(buildSnapshotEvent(saved));
        }

        for (EvaluationCategoryWeightSaveRequest req : request.getCategoryWeights()) {
            EvaluationTierGroup tierGroup = EvaluationTierGroup.valueOf(req.getTierGroup());
            EvaluationCategory categoryCode = EvaluationCategory.valueOf(req.getCategoryCode());

            evaluationWeightConfigRepository.findByTierGroupAndCategoryCodeAndActiveTrueAndDeletedFalse(tierGroup, categoryCode)
                .ifPresent(existing -> {
                    existing.deactivate();
                    EvaluationWeightConfig deactivated = evaluationWeightConfigRepository.save(existing);
                    weightSnapshotEvents.add(buildWeightSnapshotEvent(deactivated));
                });

            EvaluationWeightConfig saved = evaluationWeightConfigRepository.save(EvaluationWeightConfig.builder()
                .evaluationWeightConfigId(idGenerator.generate())
                .tierGroup(tierGroup)
                .categoryCode(categoryCode)
                .weightPercent(req.getWeightPercent())
                .active(Boolean.TRUE)
                .deleted(Boolean.FALSE)
                .build());
            weightSnapshotEvents.add(buildWeightSnapshotEvent(saved));
        }

        publishSnapshotsAfterCommit(snapshotEvents, weightSnapshotEvents);
    }

    private void validateWeightTotals(List<EvaluationCategoryWeightSaveRequest> requests) {
        Map<EvaluationTierGroup, Integer> totals = new EnumMap<>(EvaluationTierGroup.class);

        for (EvaluationCategoryWeightSaveRequest request : requests) {
            EvaluationTierGroup tierGroup = EvaluationTierGroup.valueOf(request.getTierGroup());
            totals.merge(tierGroup, request.getWeightPercent(), Integer::sum);
        }

        for (EvaluationTierGroup tierGroup : EvaluationTierGroup.values()) {
            if (totals.getOrDefault(tierGroup, 0) != 100) {
                throw new IllegalArgumentException("Weight total for " + tierGroup + " must be 100");
            }
        }
    }

    private void publishSnapshotsAfterCommit(
        List<TierConfigSnapshotEvent> snapshotEvents,
        List<EvaluationWeightConfigSnapshotEvent> weightSnapshotEvents
    ) {
        if (snapshotEvents.isEmpty() && weightSnapshotEvents.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    snapshotEvents.forEach(promotionEventPublisher::publishTierConfigSnapshot);
                    weightSnapshotEvents.forEach(promotionEventPublisher::publishEvaluationWeightConfigSnapshot);
                }
            });
            return;
        }

        snapshotEvents.forEach(promotionEventPublisher::publishTierConfigSnapshot);
        weightSnapshotEvents.forEach(promotionEventPublisher::publishEvaluationWeightConfigSnapshot);
    }

    private TierConfigSnapshotEvent buildSnapshotEvent(TierConfig tierConfig) {
        return TierConfigSnapshotEvent.builder()
            .tierConfigId(tierConfig.getTierConfigId())
            .tier(tierConfig.getTierConfigTier().name())
            .weightDistribution(null)
            .promotionPoint(tierConfig.getTierConfigPromotionPoint())
            .active(tierConfig.getActive())
            .deleted(tierConfig.getDeleted())
            .occurredAt(LocalDateTime.now())
            .build();
    }

    private EvaluationWeightConfigSnapshotEvent buildWeightSnapshotEvent(EvaluationWeightConfig config) {
        return EvaluationWeightConfigSnapshotEvent.builder()
            .evaluationWeightConfigId(config.getEvaluationWeightConfigId())
            .tierGroup(config.getTierGroup().name())
            .categoryCode(config.getCategoryCode().name())
            .weightPercent(config.getWeightPercent())
            .active(config.getActive())
            .deleted(config.getDeleted())
            .occurredAt(LocalDateTime.now())
            .build();
    }
}
