package com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.EvaluationWeightConfigSnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PerformancePointSnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PromotionHistorySnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.TierConfigSnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.PromotionKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PromotionEventPublisher.class);

    private final KafkaTemplate<String, PerformancePointSnapshotEvent> performancePointSnapshotKafkaTemplate;
    private final KafkaTemplate<String, PromotionHistorySnapshotEvent> promotionHistorySnapshotKafkaTemplate;
    private final KafkaTemplate<String, TierConfigSnapshotEvent> tierConfigSnapshotKafkaTemplate;
    private final KafkaTemplate<String, EvaluationWeightConfigSnapshotEvent> evaluationWeightConfigSnapshotKafkaTemplate;

    public void publishPerformancePointSnapshot(PerformancePointSnapshotEvent event) {
        performancePointSnapshotKafkaTemplate.send(
            PromotionKafkaTopics.PERFORMANCE_POINT_SNAPSHOT,
            String.valueOf(event.getPerformancePointId()),
            event
        );
        log.info(
            "Published performance point snapshot. pointId={}, employeeId={}, pointType={}, pointAmount={}",
            event.getPerformancePointId(),
            event.getEmployeeId(),
            event.getPointType(),
            event.getPointAmount()
        );
    }

    public void publishPromotionHistorySnapshot(PromotionHistorySnapshotEvent event) {
        promotionHistorySnapshotKafkaTemplate.send(
            PromotionKafkaTopics.PROMOTION_HISTORY_SNAPSHOT,
            String.valueOf(event.getTierPromotionId()),
            event
        );
        log.info(
            "Published promotion history snapshot. promotionId={}, employeeId={}, status={}",
            event.getTierPromotionId(),
            event.getEmployeeId(),
            event.getPromotionStatus()
        );
    }

    public void publishTierConfigSnapshot(TierConfigSnapshotEvent event) {
        tierConfigSnapshotKafkaTemplate.send(
            PromotionKafkaTopics.TIER_CONFIG_SNAPSHOT,
            String.valueOf(event.getTierConfigId()),
            event
        );
        log.info(
            "Published tier config snapshot. tierConfigId={}, tier={}, promotionPoint={}, active={}, deleted={}",
            event.getTierConfigId(),
            event.getTier(),
            event.getPromotionPoint(),
            event.getActive(),
            event.getDeleted()
        );
    }

    public void publishEvaluationWeightConfigSnapshot(EvaluationWeightConfigSnapshotEvent event) {
        evaluationWeightConfigSnapshotKafkaTemplate.send(
            PromotionKafkaTopics.EVALUATION_WEIGHT_CONFIG_SNAPSHOT,
            String.valueOf(event.getEvaluationWeightConfigId()),
            event
        );
        log.info(
            "Published evaluation weight config snapshot. configId={}, tierGroup={}, categoryCode={}, weightPercent={}, active={}, deleted={}",
            event.getEvaluationWeightConfigId(),
            event.getTierGroup(),
            event.getCategoryCode(),
            event.getWeightPercent(),
            event.getActive(),
            event.getDeleted()
        );
    }
}
