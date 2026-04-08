package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.TierCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.TierConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TierCriteriaCommandService {

    private final TierConfigRepository tierConfigRepository;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public void saveCriteria(List<TierCriteriaSaveRequest> requests) {
        for (TierCriteriaSaveRequest req : requests) {
            validateWeightSum(req.getTierConfigWeightDistribution());

            TierConfig config = TierConfig.builder()
                    .tierConfigId(idGenerator.generate())
                    .tierConfigTier(Grade.valueOf(req.getTier()))
                    .tierConfigWeightDistribution(req.getTierConfigWeightDistribution())
                    .tierConfigPromotionPoint(req.getTierConfigPromotionPoint())
                    .build();
            tierConfigRepository.save(config);
        }
    }

    private void validateWeightSum(String weightDistributionJson) {
        try {
            Map<String, Integer> weights = objectMapper.readValue(
                    weightDistributionJson, new TypeReference<>() {});
            int sum = weights.values().stream().mapToInt(Integer::intValue).sum();
            if (sum != 100) {
                throw new IllegalArgumentException(
                        "가중치 합계는 100%이어야 합니다. 현재 합계: " + sum);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("가중치 분포 JSON 형식이 올바르지 않습니다.");
        }
    }
}
