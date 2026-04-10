package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendhr.hr.command.application.service.PromotionCandidateCommandService;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PromotionCandidateEvaluatedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.PromotionKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionCandidateEvaluatedListener {

    private static final Logger log = LoggerFactory.getLogger(PromotionCandidateEvaluatedListener.class);

    private final PromotionCandidateCommandService promotionCandidateCommandService;

    @KafkaListener(
        topics = PromotionKafkaTopics.PROMOTION_CANDIDATE_EVALUATED,
        containerFactory = "promotionCandidateEvaluatedKafkaListenerContainerFactory"
    )
    public void listen(PromotionCandidateEvaluatedEvent event) {
        log.info(
            "Received promotion candidate evaluated event. employeeId={}, currentTier={}, targetTier={}, accumulatedPoint={}",
            event.getEmployeeId(),
            event.getCurrentTier(),
            event.getTargetTier(),
            event.getTierAccumulatedPoint()
        );
        promotionCandidateCommandService.applyCandidate(event);
    }
}
