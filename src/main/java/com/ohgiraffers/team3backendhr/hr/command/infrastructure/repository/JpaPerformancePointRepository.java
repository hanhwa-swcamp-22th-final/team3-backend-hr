package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.performancepoint.PerformancePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaPerformancePointRepository extends JpaRepository<PerformancePoint, Long> {
    List<PerformancePoint> findByPerformanceEmployeeId(Long employeeId);
}
