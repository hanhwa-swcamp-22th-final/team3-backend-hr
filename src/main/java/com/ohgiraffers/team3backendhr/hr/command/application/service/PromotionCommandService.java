package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PromotionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionCommandService {

    private final PromotionHistoryRepository promotionHistoryRepository;

    public void confirmPromotion(Long tierPromotionId) {
        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId)
                .orElseThrow(() -> new IllegalArgumentException("승급 이력을 찾을 수 없습니다."));
        history.confirm();
    }

    public void suspendPromotion(Long tierPromotionId) {
        PromotionHistory history = promotionHistoryRepository.findById(tierPromotionId)
                .orElseThrow(() -> new IllegalArgumentException("승급 이력을 찾을 수 없습니다."));
        history.suspend();
    }
}
