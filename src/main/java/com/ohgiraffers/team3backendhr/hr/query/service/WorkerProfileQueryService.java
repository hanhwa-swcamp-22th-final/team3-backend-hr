package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.infrastructure.client.ScmClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierChartPointResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerTaskSummaryResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkerProfileQueryService {

    private final AdminClient adminClient;
    private final ScmClient scmClient;

    /* HR-064: 내 프로필 조회 */
    public EmployeeProfileResponse getProfile(Long employeeId) {
        EmployeeProfileResponse profile = adminClient.getWorkerProfile(employeeId);
        WorkerTaskSummaryResponse summary = getTaskSummary(employeeId);
        return enrichTaskSummary(profile, summary);
    }

    /* HR-065: 내 스킬 조회 */
    public List<EmployeeSkillResponse> getSkills(Long employeeId) {
        return adminClient.getWorkerSkills(employeeId);
    }

    /* HR-066: Tier 차트 조회 */
    public List<TierChartPointResponse> getTierChart(Long employeeId) {
        return adminClient.getTierChart(employeeId);
    }

    private WorkerTaskSummaryResponse getTaskSummary(Long employeeId) {
        try {
            return scmClient.getWorkerTaskSummary(employeeId);
        } catch (FeignException e) {
            log.warn("Failed to fetch SCM worker task summary. employeeId={}, status={}", employeeId, e.status());
            return null;
        }
    }

    private EmployeeProfileResponse enrichTaskSummary(
        EmployeeProfileResponse profile,
        WorkerTaskSummaryResponse summary
    ) {
        if (profile == null) {
            return null;
        }

        int assignedCount = valueOrZero(summary == null ? null : summary.getAssignedCount());
        int inProgressCount = valueOrZero(summary == null ? null : summary.getInProgressCount());
        int completedCount = valueOrZero(summary == null ? null : summary.getCompletedCount());
        int denominator = assignedCount + inProgressCount + completedCount;
        int completionRate = denominator == 0 ? 0 : Math.round((completedCount * 100.0f) / denominator);

        return EmployeeProfileResponse.builder()
            .employeeId(profile.getEmployeeId())
            .employeeCode(profile.getEmployeeCode())
            .employeeName(profile.getEmployeeName())
            .departmentName(profile.getDepartmentName())
            .teamName(profile.getTeamName())
            .currentTier(profile.getCurrentTier())
            .totalScore(profile.getTotalScore())
            .hireDate(profile.getHireDate())
            .assignedTaskCount(assignedCount)
            .inProgressTaskCount(inProgressCount)
            .completedTaskCount(completedCount)
            .taskCompletionRate(completionRate)
            .build();
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
