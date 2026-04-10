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

    @NotNull
    private final Double equipmentResponseTargetScore;

    @NotNull
    private final Double technicalTransferTargetScore;

    @NotNull
    private final Double innovationProposalTargetScore;

    @NotNull
    private final Double safetyComplianceTargetScore;

    @NotNull
    private final Double qualityManagementTargetScore;

    @NotNull
    private final Double productivityTargetScore;
}
