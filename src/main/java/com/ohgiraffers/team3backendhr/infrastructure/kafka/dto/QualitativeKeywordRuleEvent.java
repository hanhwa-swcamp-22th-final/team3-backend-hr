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

    private Long domainKeywordId;
    private String keyword;
    private String domainCompetencyCategory;
    private BigDecimal scoreWeight;
}
