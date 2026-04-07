package com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationSubmittedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.QualitativeKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QualitativeEvaluationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(QualitativeEvaluationEventPublisher.class);

    private final KafkaTemplate<String, QualitativeEvaluationSubmittedEvent> qualitativeSubmittedKafkaTemplate;

    public void publishSubmitted(QualitativeEvaluationSubmittedEvent event) {
        qualitativeSubmittedKafkaTemplate.send(
            QualitativeKafkaTopics.QUALITATIVE_EVALUATION_SUBMITTED,
            String.valueOf(event.getQualitativeEvaluationId()),
            event
        );
        log.info(
            "Published qualitative submitted event. evaluationId={}, evaluateeId={}, periodId={}",
            event.getQualitativeEvaluationId(),
            event.getEvaluateeId(),
            event.getEvaluationPeriodId()
        );
    }
}