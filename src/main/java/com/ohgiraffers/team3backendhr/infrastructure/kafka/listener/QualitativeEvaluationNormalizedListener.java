package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendhr.hr.command.application.service.QualitativeEvaluationCommandService;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationNormalizedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.QualitativeKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QualitativeEvaluationNormalizedListener {

    private static final Logger log = LoggerFactory.getLogger(QualitativeEvaluationNormalizedListener.class);

    private final QualitativeEvaluationCommandService qualitativeEvaluationCommandService;

    @KafkaListener(
        topics = QualitativeKafkaTopics.QUALITATIVE_EVALUATION_NORMALIZED,
        containerFactory = "qualitativeNormalizedKafkaListenerContainerFactory"
    )
    public void listen(QualitativeEvaluationNormalizedEvent event) {
        log.info(
            "Received qualitative normalized event. evaluationId={}, rawScore={}, sQual={}, grade={}",
            event.getQualitativeEvaluationId(),
            event.getRawScore(),
            event.getSQual(),
            event.getGrade()
        );

        qualitativeEvaluationCommandService.applyNormalizedResult(event);
    }
}
