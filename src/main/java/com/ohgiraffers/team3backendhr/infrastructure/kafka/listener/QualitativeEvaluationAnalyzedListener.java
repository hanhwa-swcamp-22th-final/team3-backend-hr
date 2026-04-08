package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendhr.hr.command.application.service.QualitativeEvaluationCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationAnalyzedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.QualitativeKafkaTopics;
import java.math.BigDecimal;
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
            "Received qualitative analyzed event. evaluationId={}, status={}, sQual={}, tier={}",
            event.getQualitativeEvaluationId(),
            event.getAnalysisStatus(),
            event.getSQual(),
            event.getNormalizedTier()
        );

        BigDecimal score = event.getSQual();
        String normalizedTier = event.getNormalizedTier();
        if (score == null || normalizedTier == null || normalizedTier.isBlank()) {
            log.warn(
                "Skipping qualitative analyzed event without finalized batch result. evaluationId={}, status={}, sQual={}, tier={}",
                event.getQualitativeEvaluationId(),
                event.getAnalysisStatus(),
                score,
                normalizedTier
            );
            return;
        }

        qualitativeEvaluationCommandService.applyAnalysisResult(
            event.getQualitativeEvaluationId(),
            score.doubleValue(),
            Grade.valueOf(normalizedTier)
        );
    }
}