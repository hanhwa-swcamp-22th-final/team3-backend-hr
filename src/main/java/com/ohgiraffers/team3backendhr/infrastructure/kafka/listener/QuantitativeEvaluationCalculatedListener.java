package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendhr.hr.command.application.service.QuantitativeEvaluationCommandService;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QuantitativeEvaluationCalculatedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.QuantitativeKafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuantitativeEvaluationCalculatedListener {

    private final QuantitativeEvaluationCommandService service;

    @KafkaListener(
            topics = QuantitativeKafkaTopics.QUANTITATIVE_EVALUATION_CALCULATED,
            containerFactory = "quantitativeCalculatedKafkaListenerContainerFactory"
    )
    public void listen(QuantitativeEvaluationCalculatedEvent event) {
        log.info("Received quantitative calculated event. employeeId={}, periodId={}, equipmentId={}",
                event.getEmployeeId(), event.getEvalPeriodId(), event.getEquipmentId());

        if (event.getEmployeeId() == null || event.getEvalPeriodId() == null || event.getEquipmentId() == null) {
            log.warn("Skipping quantitative event - missing required fields. event={}", event);
            return;
        }

        service.applyBatchResult(
                event.getEmployeeId(),
                event.getEvalPeriodId(),
                event.getEquipmentId(),
                event.getUphScore(),
                event.getYieldScore(),
                event.getLeadTimeScore(),
                event.getActualError(),
                event.getSQuant(),
                event.getTScore(),
                event.getMaterialShielding()
        );
    }
}
