package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_template")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MissionTemplate {

    @Id
    @Column(name = "mission_template_id")
    private Long missionTemplateId;

    @Column(name = "mission_name", nullable = false, length = 100)
    private String missionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", nullable = false)
    private MissionType missionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "upgrade_to_tier", nullable = false)
    private UpgradeToTier upgradeToTier;

    @Column(name = "condition_value", nullable = false)
    private BigDecimal conditionValue;

    @Column(name = "reward_point", nullable = false)
    private Integer rewardPoint;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

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

    /* currentValue 가 conditionValue 이상이면 달성 */
    public boolean isAchieved(BigDecimal currentValue) {
        return currentValue.compareTo(this.conditionValue) >= 0;
    }

    /* 진행률(%) 반환 — 최대 100 */
    public int calculateProgressRate(BigDecimal currentValue) {
        if (this.conditionValue.compareTo(BigDecimal.ZERO) == 0) {
            return 100;
        }
        BigDecimal rate = currentValue
                .multiply(BigDecimal.valueOf(100))
                .divide(this.conditionValue, 0, RoundingMode.FLOOR);
        return Math.min(rate.intValue(), 100);
    }
}
