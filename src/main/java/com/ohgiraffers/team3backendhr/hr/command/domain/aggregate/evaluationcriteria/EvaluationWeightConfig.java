package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcriteria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "evaluation_weight_config")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EvaluationWeightConfig {

    @Id
    @Column(name = "evaluation_weight_config_id")
    private Long evaluationWeightConfigId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_group", nullable = false, length = 10)
    private EvaluationTierGroup tierGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_code", nullable = false, length = 50)
    private EvaluationCategory categoryCode;

    @Column(name = "weight_percent", nullable = false)
    private Integer weightPercent;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted;

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

    public void deactivate() {
        this.active = Boolean.FALSE;
    }

    public void softDelete() {
        this.active = Boolean.FALSE;
        this.deleted = Boolean.TRUE;
    }
}
