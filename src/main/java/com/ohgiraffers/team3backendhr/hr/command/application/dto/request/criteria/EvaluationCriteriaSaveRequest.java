package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvaluationCriteriaSaveRequest {

    @Valid
    @NotEmpty
    private final List<TierCriteriaSaveRequest> tierConfigs;

    @Valid
    @NotEmpty
    private final List<EvaluationCategoryWeightSaveRequest> categoryWeights;
}
