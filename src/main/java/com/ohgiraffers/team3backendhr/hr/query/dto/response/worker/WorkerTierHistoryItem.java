package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkerTierHistoryItem {

    private String eventType;
    private String fromTier;
    private String toTier;
    private Integer tierAccumulatedPoint;
    private Integer targetPromotionPoint;
    private String status;
    private LocalDate occurredDate;
    private LocalDateTime occurredAt;
}
