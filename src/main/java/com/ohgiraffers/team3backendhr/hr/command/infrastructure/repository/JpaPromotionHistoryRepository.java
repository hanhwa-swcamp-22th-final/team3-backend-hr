package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPromotionHistoryRepository extends JpaRepository<PromotionHistory, Long> {
    List<PromotionHistory> findByEmployeeId(Long employeeId);

    List<PromotionHistory> findByTierPromoStatus(PromotionStatus status);

    Optional<PromotionHistory> findFirstByEmployeeIdAndCurrentTierConfigIdAndTargetTierConfigIdAndTierPromoStatusIn(
        Long employeeId,
        Long currentTierConfigId,
        Long targetTierConfigId,
        Collection<PromotionStatus> statuses
    );
}
