package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TierCriteriaSaveRequest {

    @NotBlank
    private final String tier;                          // S | A | B | C

    @NotNull
    private final Integer tierConfigPromotionPoint;
}
