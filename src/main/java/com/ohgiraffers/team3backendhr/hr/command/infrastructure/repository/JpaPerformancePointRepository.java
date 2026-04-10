package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.performancepoint.PerformancePoint;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.performancepoint.PointType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPerformancePointRepository extends JpaRepository<PerformancePoint, Long> {
    List<PerformancePoint> findByPerformanceEmployeeId(Long employeeId);

    Optional<PerformancePoint> findByPerformanceEmployeeIdAndPointTypeAndPointSourceIdAndPointSourceType(
        Long employeeId,
        PointType pointType,
        Long pointSourceId,
        String pointSourceType
    );
}
