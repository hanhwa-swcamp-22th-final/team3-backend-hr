package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.PromotionHistory;
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

    /* 승급 확정 — PromotionHistory 상태를 CONFIRMATION_OF_PROMOTION 으로 변경하고
     * Admin 서비스에 사원의 현재 티어 갱신을 요청한다. */
    public void confirmPromotion(Long tierPromotionId) {
        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId)
                .orElseThrow(() -> new IllegalArgumentException("승급 이력을 찾을 수 없습니다."));
        history.confirm();

        TierConfig targetTierConfig = tierConfigRepository.findById(history.getTargetTierConfigId())
                .orElseThrow(() -> new IllegalArgumentException("승급 목표 티어 설정을 찾을 수 없습니다."));
        Grade newTier = targetTierConfig.getTierConfigTier();

        adminClient.updateEmployeeTier(history.getEmployeeId(), newTier);
    }

    public void suspendPromotion(Long tierPromotionId) {
        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId)
                .orElseThrow(() -> new IllegalArgumentException("승급 이력을 찾을 수 없습니다."));
        history.suspend();
    }
}
