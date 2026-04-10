package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendhr.hr.command.application.service.PerformancePointCommandService;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.PromotionKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerformancePointCalculatedListener {

    private static final Logger log = LoggerFactory.getLogger(PerformancePointCalculatedListener.class);

    private final PerformancePointCommandService performancePointCommandService;

    @KafkaListener(
        topics = PromotionKafkaTopics.PERFORMANCE_POINT_CALCULATED,
        containerFactory = "performancePointCalculatedKafkaListenerContainerFactory"
    )
    public void listen(PerformancePointCalculatedEvent event) {
        log.info(
            "Received performance point calculated event. employeeId={}, evaluationPeriodId={}, pointType={}, pointAmount={}",
            event.getEmployeeId(),
            event.getEvaluationPeriodId(),
            event.getPointType(),
            event.getPointAmount()
        );
        performancePointCommandService.applyCalculatedPoint(event);
    }
}
