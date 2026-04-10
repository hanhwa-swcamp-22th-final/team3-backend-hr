package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DomainKeywordRuleResponse {

    private Long domainKeywordId;
    private String domainKeyword;
    private String domainCompetencyCategory;
    private BigDecimal domainBaseScore;
    private BigDecimal domainWeight;
    private Boolean domainIsActive;
}
