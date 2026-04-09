package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendhr.hr.command.application.service.QuantitativeEvaluationCommandService;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QuantitativeEvaluationCalculatedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.QuantitativeKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuantitativeEvaluationCalculatedListener {

    private static final Logger log = LoggerFactory.getLogger(QuantitativeEvaluationCalculatedListener.class);

    private final QuantitativeEvaluationCommandService quantitativeEvaluationCommandService;

    @KafkaListener(
        topics = QuantitativeKafkaTopics.QUANTITATIVE_EVALUATION_CALCULATED,
        containerFactory = "quantitativeCalculatedKafkaListenerContainerFactory"
    )
    public void listen(QuantitativeEvaluationCalculatedEvent event) {
        log.info(
            "Received quantitative calculated event. employeeId={}, evaluationPeriodId={}, equipmentCount={}, periodType={}",
            event.getEmployeeId(),
            event.getEvaluationPeriodId(),
            event.getEquipmentResults() == null ? 0 : event.getEquipmentResults().size(),
            event.getPeriodType()
        );

        if (event.getEquipmentResults() == null || event.getEquipmentResults().isEmpty()) {
            log.warn(
                "Skipping empty quantitative calculated event. employeeId={}, evaluationPeriodId={}",
                event.getEmployeeId(),
                event.getEvaluationPeriodId()
            );
            return;
        }

        quantitativeEvaluationCommandService.applyCalculatedResult(event);
    }
}
