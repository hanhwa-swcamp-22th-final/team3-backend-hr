package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.PromotionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.TierConfig;
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
                .orElseThrow(() -> new IllegalArgumentException("승급 이력을 찾을 수 없습니다."));
        history.confirm();
    }

    /* 티어 일괄 반영 — CONFIRMATION_OF_PROMOTION 상태인 모든 이력을 Admin 서비스에 반영하고
     * 상태를 TIER_APPLIED 로 전이한다. */
    public void applyTierForConfirmed() {
        promotionHistoryRepository.findByTierPromoStatus(PromotionStatus.CONFIRMATION_OF_PROMOTION)
                .forEach(history -> {
                    TierConfig targetTierConfig = tierConfigRepository.findById(history.getTargetTierConfigId())
                            .orElseThrow(() -> new IllegalArgumentException("승급 목표 티어 설정을 찾을 수 없습니다."));
                    Grade newTier = targetTierConfig.getTierConfigTier();
                    adminClient.updateEmployeeTier(history.getEmployeeId(), newTier);
                    history.applyTier();
                });
    }

    public void suspendPromotion(Long tierPromotionId) {
        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId)
                .orElseThrow(() -> new IllegalArgumentException("승급 이력을 찾을 수 없습니다."));
        history.suspend();
    }
}
