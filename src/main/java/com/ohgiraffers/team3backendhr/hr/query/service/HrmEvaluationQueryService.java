package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.AntiGamingFlagItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.BiasReportItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.TierCriteriaItem;
import com.ohgiraffers.team3backendhr.hr.query.mapper.HrmEvaluationQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HrmEvaluationQueryService {

    private final HrmEvaluationQueryMapper mapper;
    private final TierConfigRepository tierConfigRepository;

    /* 편향 보정 이력 조회 */
    public List<BiasReportItem> getBiasReport() {
        return mapper.findBiasReport();
    }

    /* 어뷰징 감지 목록 조회 */
    public List<AntiGamingFlagItem> getAntiGamingFlags() {
        return mapper.findAntiGamingFlags();
    }

    /* 평가 기준(가중치·임계값) 조회 */
    public List<TierCriteriaItem> getCriteria() {
        return tierConfigRepository.findAll().stream()
                .map(tc -> new TierCriteriaItem(
                        tc.getTierConfigId(),
                        tc.getTierConfigTier().name(),
                        tc.getTierConfigWeightDistribution(),
                        tc.getTierConfigPromotionPoint()))
                .toList();
    }
}
