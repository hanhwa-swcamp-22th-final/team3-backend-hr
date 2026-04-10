package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.performancepoint;

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
@Table(name = "performance_point")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PerformancePoint {

    @Id
    @Column(name = "performance_point_id")
    private Long performancePointId;

    @Column(name = "performance_employee_id", nullable = false)
    private Long performanceEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type")
    private PointType pointType;

    @Column(name = "point_amount")
    private BigDecimal pointAmount;

    @Column(name = "point_earned_date")
    private LocalDate pointEarnedDate;

    @Column(name = "point_source_id")
    private Long pointSourceId;

    @Column(name = "point_source_type")
    private String pointSourceType;

    @Column(name = "point_description", length = 500)
    private String pointDescription;

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

    public void applyCalculated(
        PointType pointType,
        BigDecimal pointAmount,
        LocalDate pointEarnedDate,
        Long pointSourceId,
        String pointSourceType,
        String pointDescription
    ) {
        this.pointType = pointType;
        this.pointAmount = pointAmount;
        this.pointEarnedDate = pointEarnedDate;
        this.pointSourceId = pointSourceId;
        this.pointSourceType = pointSourceType;
        this.pointDescription = pointDescription;
    }
}
