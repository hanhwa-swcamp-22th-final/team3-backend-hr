package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.MissionTemplate;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.MissionType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.UpgradeToTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaMissionTemplateRepository extends JpaRepository<MissionTemplate, Long> {
    List<MissionTemplate> findByIsActiveTrue();
    List<MissionTemplate> findByUpgradeToTierAndIsActiveTrue(UpgradeToTier upgradeToTier);
    List<MissionTemplate> findByMissionTypeAndIsActiveTrue(MissionType missionType);
}
