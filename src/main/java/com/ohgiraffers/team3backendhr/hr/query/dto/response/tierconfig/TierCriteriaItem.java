package com.ohgiraffers.team3backendhr.hr.query.dto.response.tierconfig;

import lombok.Getter;
import org.apache.ibatis.annotations.AutomapConstructor;

@Getter
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

    @AutomapConstructor
    public TierCriteriaItem(Long tierConfigId, String tier, Integer tierConfigPromotionPoint) {
        this(
            tierConfigId,
            tier,
            tierConfigPromotionPoint,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public TierCriteriaItem(
        Long tierConfigId,
        String tier,
        Integer tierConfigPromotionPoint,
        Double equipmentResponseTargetScore,
        Double technicalTransferTargetScore,
        Double innovationProposalTargetScore,
        Double safetyComplianceTargetScore,
        Double qualityManagementTargetScore,
        Double productivityTargetScore
    ) {
        this.tierConfigId = tierConfigId;
        this.tier = tier;
        this.tierConfigPromotionPoint = tierConfigPromotionPoint;
        this.equipmentResponseTargetScore = equipmentResponseTargetScore;
        this.technicalTransferTargetScore = technicalTransferTargetScore;
        this.innovationProposalTargetScore = innovationProposalTargetScore;
        this.safetyComplianceTargetScore = safetyComplianceTargetScore;
        this.qualityManagementTargetScore = qualityManagementTargetScore;
        this.productivityTargetScore = productivityTargetScore;
    }
}
