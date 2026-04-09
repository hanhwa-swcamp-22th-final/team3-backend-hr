package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuantEvalScores {

    private final Double uphScore;
    private final Double yieldScore;
    private final Double leadTimeScore;
    private final Double actualError;
    private final Double sQuant;
    private final Double tScore;
    private final Boolean materialShielding;
}
