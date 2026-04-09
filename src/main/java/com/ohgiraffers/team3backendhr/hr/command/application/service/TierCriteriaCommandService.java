package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.TierCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TierCriteriaCommandService {

    private final TierConfigRepository tierConfigRepository;
    private final IdGenerator idGenerator;

    public void saveCriteria(List<TierCriteriaSaveRequest> requests) {
        for (TierCriteriaSaveRequest req : requests) {
            TierConfig config = TierConfig.builder()
                    .tierConfigId(idGenerator.generate())
                    .tierConfigTier(Grade.valueOf(req.getTier()))
                    .tierConfigPromotionPoint(req.getTierConfigPromotionPoint())
                    .equipmentResponseTargetScore(req.getEquipmentResponseTargetScore())
                    .technicalTransferTargetScore(req.getTechnicalTransferTargetScore())
                    .innovationProposalTargetScore(req.getInnovationProposalTargetScore())
                    .safetyComplianceTargetScore(req.getSafetyComplianceTargetScore())
                    .qualityManagementTargetScore(req.getQualityManagementTargetScore())
                    .productivityTargetScore(req.getProductivityTargetScore())
                    .build();
            tierConfigRepository.save(config);
        }
    }
}
