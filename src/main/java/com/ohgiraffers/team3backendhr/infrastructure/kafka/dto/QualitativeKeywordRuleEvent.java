package com.ohgiraffers.team3backendhr.infrastructure.kafka.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualitativeKeywordRuleEvent {

    private String keyword;
    private BigDecimal scoreWeight;
}