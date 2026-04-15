package com.ohgiraffers.team3backendhr.hr.command.application.dto.response.criteria;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvaluationCriteriaCommandResponse {

    private final List<Long> tierConfigIds;
    private final List<Long> evaluationWeightConfigIds;
    private final int tierConfigCount;
    private final int evaluationWeightConfigCount;
}
