package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

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
        // TODO ?袁⑹뒄 ??HR 鈺곌퀬??筌뤴뫀??揶쏄퉮?? ???뵝 獄쏆뮉六? ?遺얇늺 揶쏄퉮???紐꺿봺椰꾧퀡以??類ㅼ삢??뺣뼄.
    }
}
