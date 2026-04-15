package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.response.mission.MissionAssignmentResponse;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missionprogress.MissionProgress;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.MissionTemplate;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.UpgradeToTier;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.MissionProgressRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.MissionTemplateRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionAssignmentCommandService {

    private final MissionTemplateRepository missionTemplateRepository;
    private final MissionProgressRepository missionProgressRepository;
    private final AdminClient adminClient;
    private final IdGenerator idGenerator;

    @Transactional
    public MissionAssignmentResponse assignNextTierMissions(Long employeeId, String currentTier) {
        UpgradeToTier upgradeToTier = nextUpgradeTier(currentTier);
        if (upgradeToTier == null
                || !adminClient.existsActiveWorkerByIdAndTier(employeeId, currentTier)) {
            return MissionAssignmentResponse.builder()
                    .employeeId(employeeId)
                    .upgradeToTier(upgradeToTier == null ? null : upgradeToTier.getDbValue())
                    .createdProgressCount(0)
                    .skippedProgressCount(0)
                    .build();
        }

        AssignmentResult result = assignTemplatesToEmployees(
                missionTemplateRepository.findByUpgradeToTierAndIsActiveTrue(upgradeToTier),
                List.of(employeeId),
                BigDecimal.ZERO
        );

        return MissionAssignmentResponse.builder()
                .employeeId(employeeId)
                .upgradeToTier(upgradeToTier.getDbValue())
                .createdProgressCount(result.createdCount())
                .skippedProgressCount(result.skippedCount())
                .build();
    }

    AssignmentResult assignTemplateToEligibleWorkers(MissionTemplate template, BigDecimal initialValue) {
        String previousTier = previousTier(template.getUpgradeToTier());
        if (previousTier == null) {
            return new AssignmentResult(0, 0);
        }

        List<Long> employeeIds = adminClient.getActiveWorkerIdsByTier(previousTier);
        return assignTemplatesToEmployees(List.of(template), employeeIds, initialValue);
    }

    AssignmentResult assignTemplatesToEmployees(
            List<MissionTemplate> templates,
            List<Long> employeeIds,
            BigDecimal initialValue
    ) {
        int createdCount = 0;
        int skippedCount = 0;

        for (MissionTemplate template : templates) {
            for (Long employeeId : employeeIds) {
                if (missionProgressRepository
                        .findByEmployeeIdAndMissionTemplateId(employeeId, template.getMissionTemplateId())
                        .isPresent()) {
                    skippedCount++;
                    continue;
                }

                missionProgressRepository.save(MissionProgress.builder()
                        .missionProgressId(idGenerator.generate())
                        .employeeId(employeeId)
                        .missionTemplateId(template.getMissionTemplateId())
                        .currentValue(initialValue == null ? BigDecimal.ZERO : initialValue)
                        .build());
                createdCount++;
            }
        }

        return new AssignmentResult(createdCount, skippedCount);
    }

    private UpgradeToTier nextUpgradeTier(String currentTier) {
        if (currentTier == null) {
            return null;
        }

        return switch (currentTier) {
            case "C" -> UpgradeToTier.B;
            case "B" -> UpgradeToTier.A;
            case "A" -> UpgradeToTier.S;
            case "S" -> UpgradeToTier.S_PLUS;
            default -> null;
        };
    }

    private String previousTier(UpgradeToTier upgradeToTier) {
        return switch (upgradeToTier) {
            case B -> "C";
            case A -> "B";
            case S -> "A";
            case S_PLUS -> "S";
        };
    }

    record AssignmentResult(int createdCount, int skippedCount) {
    }
}
