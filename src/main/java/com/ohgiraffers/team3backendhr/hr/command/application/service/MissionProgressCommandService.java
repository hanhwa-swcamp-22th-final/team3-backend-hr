package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missionprogress.MissionProgress;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missionprogress.MissionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.MissionTemplate;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.MissionType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.performancepoint.PerformancePoint;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.performancepoint.PointType;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.MissionProgressRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.MissionTemplateRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PerformancePointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionProgressCommandService {

    private final MissionTemplateRepository missionTemplateRepository;
    private final MissionProgressRepository missionProgressRepository;
    private final PerformancePointRepository performancePointRepository;

    /**
     * 미션 진행값 갱신.
     * <p>완료 조건 충족 시 status → COMPLETED + 보상 포인트 적립.</p>
     *
     * @param employeeId    대상 사원 ID
     * @param missionType   미션 유형
     * @param progressValue 증가량(카운트 기반) 또는 절댓값(AI_SCORE)
     * @param absolute      true 면 덮어쓰기, false 면 누적
     */
    @Transactional
    public void updateProgress(Long employeeId, MissionType missionType,
                               BigDecimal progressValue, boolean absolute) {

        List<MissionTemplate> templates =
                missionTemplateRepository.findByMissionTypeAndIsActiveTrue(missionType);

        for (MissionTemplate template : templates) {
            missionProgressRepository
                    .findByEmployeeIdAndMissionTemplateId(employeeId, template.getMissionTemplateId())
                    .ifPresent(progress -> processProgress(progress, template, progressValue, absolute));
        }
    }

    private void processProgress(MissionProgress progress, MissionTemplate template,
                                 BigDecimal progressValue, boolean absolute) {
        if (progress.getStatus() == MissionStatus.COMPLETED) {
            return;
        }

        BigDecimal newValue = absolute
                ? progressValue
                : progress.getCurrentValue().add(progressValue);

        boolean wasInProgress = progress.getStatus() == MissionStatus.IN_PROGRESS;
        progress.updateProgress(newValue, template.getConditionValue());

        if (wasInProgress && progress.getStatus() == MissionStatus.COMPLETED) {
            awardPoints(progress.getEmployeeId(), template);
            log.info("[Mission] 완료 — employeeId={}, template={}, reward={}pt",
                    progress.getEmployeeId(), template.getMissionName(), template.getRewardPoint());
        }

        missionProgressRepository.save(progress);
    }

    private void awardPoints(Long employeeId, MissionTemplate template) {
        PointType pointType = resolvePointType(template.getMissionType());
        PerformancePoint point = PerformancePoint.builder()
                .performanceEmployeeId(employeeId)
                .pointType(pointType)
                .pointAmount(BigDecimal.valueOf(template.getRewardPoint()))
                .pointEarnedDate(LocalDate.now())
                .pointSourceId(template.getMissionTemplateId())
                .pointSourceType("MISSION")
                .pointDescription(template.getMissionName() + " 미션 완료 보상")
                .build();
        performancePointRepository.save(point);
    }

    private PointType resolvePointType(MissionType missionType) {
        return switch (missionType) {
            case HIGH_DIFFICULTY_WORK -> PointType.CHALLENGE;
            case KMS_CONTRIBUTION     -> PointType.KNOWLEDGE_SHARING;
            case AI_SCORE             -> PointType.QUALITATIVE;
        };
    }
}
