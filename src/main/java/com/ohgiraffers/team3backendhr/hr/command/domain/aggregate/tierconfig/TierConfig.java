package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tier_config")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TierConfig {

    @Id
    @Column(name = "tier_config_id")
    private Long tierConfigId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_config_tier", nullable = false)
    private Grade tierConfigTier;

    @Column(name = "tier_config_promotion_point")
    private Integer tierConfigPromotionPoint;

    @Column(name = "equipment_response_target_score")
    private Double equipmentResponseTargetScore;

    @Column(name = "technical_transfer_target_score")
    private Double technicalTransferTargetScore;

    @Column(name = "innovation_proposal_target_score")
    private Double innovationProposalTargetScore;

    @Column(name = "safety_compliance_target_score")
    private Double safetyComplianceTargetScore;

    @Column(name = "quality_management_target_score")
    private Double qualityManagementTargetScore;

    @Column(name = "productivity_target_score")
    private Double productivityTargetScore;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;
}
