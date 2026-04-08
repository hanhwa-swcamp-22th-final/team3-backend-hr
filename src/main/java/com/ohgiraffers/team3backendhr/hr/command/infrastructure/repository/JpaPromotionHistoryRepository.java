package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaPromotionHistoryRepository extends JpaRepository<PromotionHistory, Long> {
    List<PromotionHistory> findByEmployeeId(Long employeeId);
    List<PromotionHistory> findByTierPromoStatus(PromotionStatus status);
}
