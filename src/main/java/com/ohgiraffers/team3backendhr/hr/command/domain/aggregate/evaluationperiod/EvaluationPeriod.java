package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod;

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
@Table(name = "evaluation_period")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EvaluationPeriod {

    @Id
    @Column(name = "eval_period_id")
    private Long evalPeriodId;

    @Column(name = "algorithm_version_id", nullable = false)
    private Long algorithmVersionId;

    @Column(name = "eval_year", nullable = false)
    private Integer evalYear;

    @Column(name = "eval_sequence", nullable = false)
    private Integer evalSequence;

    @Enumerated(EnumType.STRING)
    @Column(name = "eval_type", nullable = false)
    private EvalType evalType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EvalPeriodStatus status = EvalPeriodStatus.IN_PROGRESS;

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

    /* 상태 전이: IN_PROGRESS → CLOSING */
    public void close() {
        if (this.status != EvalPeriodStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 평가 기간만 마감할 수 있습니다.");
        }
        this.status = EvalPeriodStatus.CLOSING;
    }

    /* 상태 전이: CLOSING → CONFIRMED */
    public void confirm() {
        if (this.status != EvalPeriodStatus.CLOSING) {
            throw new IllegalStateException("마감된 평가 기간만 확정할 수 있습니다.");
        }
        this.status = EvalPeriodStatus.CONFIRMED;
    }

    public void update(LocalDate startDate, LocalDate endDate, Long algorithmVersionId) {
        if (this.status == EvalPeriodStatus.CONFIRMED) {
            throw new IllegalStateException("확정된 평가 기간은 수정할 수 없습니다.");
        }
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
        if (algorithmVersionId != null) this.algorithmVersionId = algorithmVersionId;
    }
}
