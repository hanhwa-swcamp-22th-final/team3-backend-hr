package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTierConfigRepository extends JpaRepository<TierConfig, Long> {
    Optional<TierConfig> findByTierConfigTierAndActiveTrueAndDeletedFalse(Grade tier);

    Optional<TierConfig> findTopByTierConfigTierOrderByCreatedAtDescTierConfigIdDesc(Grade tier);

    List<TierConfig> findAllByActiveTrueAndDeletedFalse();
}
