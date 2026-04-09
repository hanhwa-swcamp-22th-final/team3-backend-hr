package com.ohgiraffers.team3backendhr.infrastructure.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuantitativeEvaluationCalculatedEvent {

    private Long employeeId;
    private Long evalPeriodId;
    private Long equipmentId;
    private Double uphScore;
    private Double yieldScore;
    private Double leadTimeScore;
    private Double actualError;
    private Double sQuant;
    private Double tScore;
    private Boolean materialShielding;
    private LocalDateTime calculatedAt;
}
