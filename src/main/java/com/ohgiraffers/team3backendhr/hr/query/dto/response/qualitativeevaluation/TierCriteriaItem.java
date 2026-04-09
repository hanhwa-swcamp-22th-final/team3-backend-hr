package com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TierCriteriaItem {

    private final Long tierConfigId;
    private final String tier;
    private final Integer tierConfigPromotionPoint;
    private final Double equipmentResponseTargetScore;
    private final Double technicalTransferTargetScore;
    private final Double innovationProposalTargetScore;
    private final Double safetyComplianceTargetScore;
    private final Double qualityManagementTargetScore;
    private final Double productivityTargetScore;
}
