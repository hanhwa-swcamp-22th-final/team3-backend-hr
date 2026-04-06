package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.MissionProgress;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaMissionProgressRepository extends JpaRepository<MissionProgress, Long> {
    List<MissionProgress> findByEmployeeId(Long employeeId);
    List<MissionProgress> findByEmployeeIdAndStatus(Long employeeId, MissionStatus status);
    Optional<MissionProgress> findByEmployeeIdAndMissionTemplateId(Long employeeId, Long missionTemplateId);
}
