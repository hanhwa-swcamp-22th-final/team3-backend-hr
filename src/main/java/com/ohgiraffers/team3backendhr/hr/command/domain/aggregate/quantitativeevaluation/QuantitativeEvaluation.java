package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation;

import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QuantitativeEquipmentResultEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quantitative_evaluation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuantitativeEvaluation {

    @Id
    @Column(name = "quantitative_evaluation_id")
    private Long quantitativeEvaluationId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "eval_period_id", nullable = false)
    private Long evaluationPeriodId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "uph_score")
    private BigDecimal uphScore;

    @Column(name = "yield_score")
    private BigDecimal yieldScore;

    @Column(name = "lead_time_score")
    private BigDecimal leadTimeScore;

    @Column(name = "actual_error")
    private BigDecimal actualError;

    @Column(name = "s_quant")
    private BigDecimal sQuant;

    @Column(name = "t_score")
    private BigDecimal tScore;

    @Column(name = "material_shielding")
    private Integer materialShielding;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static QuantitativeEvaluation create(
        Long quantitativeEvaluationId,
        Long employeeId,
        Long evaluationPeriodId,
        Long equipmentId
    ) {
        QuantitativeEvaluation evaluation = new QuantitativeEvaluation();
        evaluation.quantitativeEvaluationId = quantitativeEvaluationId;
        evaluation.employeeId = employeeId;
        evaluation.evaluationPeriodId = evaluationPeriodId;
        evaluation.equipmentId = equipmentId;
        return evaluation;
    }

    public void applyCalculatedResult(QuantitativeEquipmentResultEvent result, LocalDateTime occurredAt, Long actorId) {
        if (this.createdAt == null) {
            this.createdAt = occurredAt;
            this.createdBy = actorId;
        }

        this.uphScore = result.getUphScore();
        this.yieldScore = result.getYieldScore();
        this.leadTimeScore = result.getLeadTimeScore();
        this.actualError = result.getActualError();
        this.sQuant = result.getSQuant();
        this.tScore = result.getTScore();
        this.materialShielding = result.getMaterialShielding();
        this.status = result.getStatus();
        this.updatedAt = occurredAt;
        this.updatedBy = actorId;
    }
}
