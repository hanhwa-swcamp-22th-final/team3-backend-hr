package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendhr.hr.command.application.service.QualitativeEvaluationCommandService;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationAnalyzedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.QualitativeKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QualitativeEvaluationAnalyzedListener {

    private static final Logger log = LoggerFactory.getLogger(QualitativeEvaluationAnalyzedListener.class);

    private final QualitativeEvaluationCommandService qualitativeEvaluationCommandService;

    @KafkaListener(
        topics = QualitativeKafkaTopics.QUALITATIVE_EVALUATION_ANALYZED,
        containerFactory = "qualitativeAnalyzedKafkaListenerContainerFactory"
    )
    public void listen(QualitativeEvaluationAnalyzedEvent event) {
        log.info(
            "Received qualitative analyzed event. evaluationId={}, status={}, rawScore={}, sQual={}, tier={}, sentenceCount={}",
            event.getQualitativeEvaluationId(),
            event.getAnalysisStatus(),
            event.getRawScore(),
            event.getSQual(),
            event.getNormalizedTier(),
            event.getSentenceAnalyses() == null ? 0 : event.getSentenceAnalyses().size()
        );

        if (event.getRawScore() == null) {
            log.warn(
                "Skipping qualitative analyzed event without raw score. evaluationId={}, status={}, rawScore={}, sQual={}, tier={}",
                event.getQualitativeEvaluationId(),
                event.getAnalysisStatus(),
                event.getRawScore(),
                event.getSQual(),
                event.getNormalizedTier()
            );
            return;
        }

        qualitativeEvaluationCommandService.applyAnalyzedResult(event);
    }
}
