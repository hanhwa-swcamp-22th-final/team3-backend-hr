package com.ohgiraffers.team3backendhr.appeal.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.appeal.command.domain.aggregate.ScoreModificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaScoreModificationLogRepository extends JpaRepository<ScoreModificationLog, Long> {
}
