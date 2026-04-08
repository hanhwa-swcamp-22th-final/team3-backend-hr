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

    @Column(name = "tier_config_weight_distribution", columnDefinition = "JSON")
    private String tierConfigWeightDistribution;    // 합계 100% — JSON

    @Column(name = "tier_config_promotion_point")
    private Integer tierConfigPromotionPoint;

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
