package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_history")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PromotionHistory {

    @Id
    @Column(name = "tier_promotion_id")
    private Long tierPromotionId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "current_tier_config_id", nullable = false)
    private Long currentTierConfigId;

    @Column(name = "target_tier_config_id", nullable = false)
    private Long targetTierConfigId;

    @Column(name = "tier_config_effective_date")
    private LocalDate tierConfigEffectiveDate;

    @Column(name = "tier_accumulated_point")
    private Integer tierAccumulatedPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_promo_status", nullable = false)
    @Builder.Default
    private PromotionStatus tierPromoStatus = PromotionStatus.UNDER_REVIEW;

    @Column(name = "tier_reviewed_at")
    private LocalDateTime tierReviewedAt;

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

    /* 상태 전이: UNDER_REVIEW → CONFIRMATION_OF_PROMOTION */
    public void confirm() {
        if (this.tierPromoStatus != PromotionStatus.UNDER_REVIEW) {
            throw new IllegalStateException("심사 중인 승급 후보만 확정할 수 있습니다.");
        }
        this.tierPromoStatus = PromotionStatus.CONFIRMATION_OF_PROMOTION;
        this.tierReviewedAt = LocalDateTime.now();
    }

    /* 상태 전이: UNDER_REVIEW → SUSPENSION */
    public void suspend() {
        if (this.tierPromoStatus != PromotionStatus.UNDER_REVIEW) {
            throw new IllegalStateException("심사 중인 승급 후보만 보류할 수 있습니다.");
        }
        this.tierPromoStatus = PromotionStatus.SUSPENSION;
        this.tierReviewedAt = LocalDateTime.now();
    }
}
