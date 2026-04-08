package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaTierConfigRepository extends JpaRepository<TierConfig, Long> {
    Optional<TierConfig> findByTierConfigTier(Grade tier);
}
