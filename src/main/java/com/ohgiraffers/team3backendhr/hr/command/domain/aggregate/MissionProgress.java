package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_progress")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MissionProgress {

    @Id
    @Column(name = "mission_progress_id")
    private Long missionProgressId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "mission_template_id", nullable = false)
    private Long missionTemplateId;

    @Column(name = "current_value", nullable = false)
    @Builder.Default
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MissionStatus status = MissionStatus.IN_PROGRESS;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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

    /* 진행값 갱신 — conditionValue 이상이면 자동으로 COMPLETED 전이 */
    public void updateProgress(BigDecimal newValue, BigDecimal conditionValue) {
        if (this.status == MissionStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 미션입니다.");
        }
        this.currentValue = newValue;
        if (newValue.compareTo(conditionValue) >= 0) {
            this.status = MissionStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }
}
