package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.mission.MissionSeedRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.response.mission.MissionSeedResponse;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.MissionTemplate;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.MissionType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.UpgradeToTier;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.MissionTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionSeedCommandService {

    private final MissionTemplateRepository missionTemplateRepository;
    private final MissionAssignmentCommandService missionAssignmentCommandService;
    private final IdGenerator idGenerator;

    @Transactional
    public MissionSeedResponse seed(MissionSeedRequest request) {
        List<Long> templateIds = new ArrayList<>();
        int createdProgressCount = 0;
        int skippedProgressCount = 0;

        for (MissionSeedRequest.MissionTemplateSeedItem item : request.getTemplates()) {
            MissionTemplate template = MissionTemplate.builder()
                    .missionTemplateId(idGenerator.generate())
                    .missionName(item.getMissionName())
                    .missionType(MissionType.valueOf(item.getMissionType()))
                    .upgradeToTier(UpgradeToTier.fromDbValue(item.getUpgradeToTier()))
                    .conditionValue(item.getConditionValue())
                    .rewardPoint(item.getRewardPoint())
                    .isActive(true)
                    .build();
            missionTemplateRepository.save(template);
            templateIds.add(template.getMissionTemplateId());

            MissionAssignmentCommandService.AssignmentResult result = resolveEmployeeIds(request).isEmpty()
                    ? missionAssignmentCommandService.assignTemplateToEligibleWorkers(template, resolveInitialValue(item))
                    : missionAssignmentCommandService.assignTemplatesToEmployees(
                            List.of(template),
                            request.getEmployeeIds(),
                            resolveInitialValue(item)
                    );
            createdProgressCount += result.createdCount();
            skippedProgressCount += result.skippedCount();
        }

        return MissionSeedResponse.builder()
                .missionTemplateIds(templateIds)
                .createdProgressCount(createdProgressCount)
                .skippedProgressCount(skippedProgressCount)
                .build();
    }

    private BigDecimal resolveInitialValue(MissionSeedRequest.MissionTemplateSeedItem item) {
        return item.getInitialValue() == null ? BigDecimal.ZERO : item.getInitialValue();
    }

    private List<Long> resolveEmployeeIds(MissionSeedRequest request) {
        return request.getEmployeeIds() == null ? List.of() : request.getEmployeeIds();
    }
}
