package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QuantitativeEvaluationRepository;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QuantitativeEvaluationCalculatedEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuantitativeEvaluationCommandService {

    private static final long SYSTEM_ACTOR_ID = 0L;

    private final QuantitativeEvaluationRepository quantitativeEvaluationRepository;
    private final IdGenerator idGenerator;

    public void applyCalculatedResult(QuantitativeEvaluationCalculatedEvent event) {
        LocalDateTime occurredAt = event.getCalculatedAt() != null ? event.getCalculatedAt() : LocalDateTime.now();
        List<QuantitativeEvaluation> evaluations = new ArrayList<>();

        event.getEquipmentResults().forEach(result -> {
            QuantitativeEvaluation evaluation = quantitativeEvaluationRepository
                .findByEmployeeIdAndEvaluationPeriodIdAndEquipmentId(
                    event.getEmployeeId(),
                    event.getEvaluationPeriodId(),
                    result.getEquipmentId()
                )
                .orElseGet(() -> QuantitativeEvaluation.create(
                    idGenerator.generate(),
                    event.getEmployeeId(),
                    event.getEvaluationPeriodId(),
                    result.getEquipmentId()
                ));

            evaluation.applyCalculatedResult(result, occurredAt, SYSTEM_ACTOR_ID);
            evaluations.add(evaluation);
        });

        quantitativeEvaluationRepository.saveAll(evaluations);
    }

}
