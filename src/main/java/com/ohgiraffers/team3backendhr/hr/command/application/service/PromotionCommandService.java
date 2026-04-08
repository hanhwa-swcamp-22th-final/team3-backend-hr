package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PromotionHistoryRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionCommandService {

    private final PromotionHistoryRepository promotionHistoryRepository;
    private final TierConfigRepository tierConfigRepository;
    private final AdminClient adminClient;

    /* 승급 확정 — PromotionHistory 상태를 CONFIRMATION_OF_PROMOTION 으로 변경한다.
     * 티어 반영은 applyTierForConfirmed() 에서 일괄 처리한다. */
    public void confirmPromotion(Long tierPromotionId) {
        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROMOTION_NOT_FOUND));
        history.confirm();
    }

    /* 티어 일괄 반영 — CONFIRMATION_OF_PROMOTION 상태인 모든 이력을 Admin 서비스에 반영하고
     * 상태를 TIER_APPLIED 로 전이한다. */
    public void applyTierForConfirmed() {
        promotionHistoryRepository.findByTierPromoStatus(PromotionStatus.CONFIRMATION_OF_PROMOTION)
                .forEach(history -> {
                    TierConfig targetTierConfig = tierConfigRepository.findById(history.getTargetTierConfigId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.TIER_CONFIG_NOT_FOUND));
                    Grade newTier = targetTierConfig.getTierConfigTier();
                    adminClient.updateEmployeeTier(history.getEmployeeId(), newTier);
                    history.applyTier();
                });
    }

    public void suspendPromotion(Long tierPromotionId) {
        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROMOTION_NOT_FOUND));
        history.suspend();
    }
}
