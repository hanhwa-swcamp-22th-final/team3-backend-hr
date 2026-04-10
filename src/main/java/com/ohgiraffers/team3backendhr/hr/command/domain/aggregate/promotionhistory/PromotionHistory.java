package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "current_tier_config_id", nullable = false)
    private Long currentTierConfigId;

    @Column(name = "target_tier_config_id", nullable = false)
    private Long targetTierConfigId;

    @Column(name = "tier_config_effective_date")
    private LocalDate tierConfigEffectiveDate;

    @Column(name = "tier_accumulated_point")
    private BigDecimal tierAccumulatedPoint;

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

    public void syncFromBatch(
        Long currentTierConfigId,
        Long targetTierConfigId,
        LocalDate tierConfigEffectiveDate,
        BigDecimal tierAccumulatedPoint,
        PromotionStatus promotionStatus
    ) {
        this.currentTierConfigId = currentTierConfigId;
        this.targetTierConfigId = targetTierConfigId;
        this.tierConfigEffectiveDate = tierConfigEffectiveDate;
        this.tierAccumulatedPoint = tierAccumulatedPoint;

        PromotionStatus requestedStatus = promotionStatus == null ? PromotionStatus.UNDER_REVIEW : promotionStatus;
        if (this.tierPromoStatus == null || this.tierPromoStatus == PromotionStatus.UNDER_REVIEW) {
            this.tierPromoStatus = requestedStatus;
            this.tierReviewedAt = requestedStatus == PromotionStatus.UNDER_REVIEW ? null : LocalDateTime.now();
        }
    }

    public void confirm(Long reviewerId) {
        if (this.tierPromoStatus != PromotionStatus.UNDER_REVIEW) {
            throw new BusinessException(ErrorCode.PROMOTION_NOT_UNDER_REVIEW);
        }
        this.reviewerId = reviewerId;
        this.tierPromoStatus = PromotionStatus.CONFIRMATION_OF_PROMOTION;
        this.tierReviewedAt = LocalDateTime.now();
    }

    public void applyTier() {
        if (this.tierPromoStatus != PromotionStatus.CONFIRMATION_OF_PROMOTION) {
            throw new BusinessException(ErrorCode.PROMOTION_NOT_CONFIRMED);
        }
        this.tierPromoStatus = PromotionStatus.TIER_APPLIED;
    }

    public void suspend(Long reviewerId) {
        if (this.tierPromoStatus != PromotionStatus.UNDER_REVIEW) {
            throw new BusinessException(ErrorCode.PROMOTION_NOT_UNDER_REVIEW);
        }
        this.reviewerId = reviewerId;
        this.tierPromoStatus = PromotionStatus.SUSPENSION;
        this.tierReviewedAt = LocalDateTime.now();
    }
}
