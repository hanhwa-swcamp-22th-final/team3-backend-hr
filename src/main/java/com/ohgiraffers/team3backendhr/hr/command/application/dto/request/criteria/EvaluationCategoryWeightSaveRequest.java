package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvaluationCategoryWeightSaveRequest {

    @NotBlank
    private final String tierGroup;

    @NotBlank
    private final String categoryCode;

    @NotNull
    @Min(0)
    @Max(100)
    private final Integer weightPercent;
}
