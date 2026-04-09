package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "quantitative_evaluation")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class QuantitativeEvaluation {

    @Id
    @Column(name = "quantitative_evaluation_id")
    private Long quantitativeEvaluationId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "eval_period_id", nullable = false)
    private Long evalPeriodId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "uph_score")
    private Double uphScore;

    @Column(name = "yield_score")
    private Double yieldScore;

    @Column(name = "lead_time_score")
    private Double leadTimeScore;

    @Column(name = "actual_error")
    private Double actualError;

    @Column(name = "s_quant")
    private Double sQuant;

    @Column(name = "t_score")
    private Double tScore;

    @Column(name = "material_shielding")
    private Boolean materialShielding;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuantEvalStatus status;

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

    /** 배치 계산 결과 반영 — CONFIRMED 상태에서는 불가 */
    public void applyBatchResult(Double uphScore, Double yieldScore, Double leadTimeScore,
                                  Double actualError, Double sQuant, Double tScore,
                                  Boolean materialShielding) {
        if (this.status == QuantEvalStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.EVALUATION_ALREADY_CONFIRMED);
        }
        this.uphScore = uphScore;
        this.yieldScore = yieldScore;
        this.leadTimeScore = leadTimeScore;
        this.actualError = actualError;
        this.sQuant = sQuant;
        this.tScore = tScore;
        this.materialShielding = materialShielding;
    }

    /** HRM 최종 확정 — TEMPORARY → CONFIRMED */
    public void confirm() {
        if (this.status == QuantEvalStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.EVALUATION_ALREADY_CONFIRMED);
        }
        this.status = QuantEvalStatus.CONFIRMED;
    }
}
