package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.ScoreModificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaScoreModificationLogRepository extends JpaRepository<ScoreModificationLog, Long> {
}
