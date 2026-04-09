package com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.EvaluationPeriodSnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.EvaluationReferenceKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvaluationReferenceEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EvaluationReferenceEventPublisher.class);

    private final KafkaTemplate<String, EvaluationPeriodSnapshotEvent> evaluationPeriodSnapshotKafkaTemplate;

    public void publishEvaluationPeriodSnapshot(EvaluationPeriodSnapshotEvent event) {
        evaluationPeriodSnapshotKafkaTemplate.send(
            EvaluationReferenceKafkaTopics.EVALUATION_PERIOD_SNAPSHOT,
            String.valueOf(event.getEvaluationPeriodId()),
            event
        );
        log.info(
            "Published evaluation period snapshot. periodId={}, algorithmVersionId={}, year={}, sequence={}",
            event.getEvaluationPeriodId(),
            event.getAlgorithmVersionId(),
            event.getEvaluationYear(),
            event.getEvaluationSequence()
        );
    }
}
