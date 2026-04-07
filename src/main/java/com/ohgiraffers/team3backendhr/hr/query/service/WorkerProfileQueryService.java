package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierChartPointResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierMilestoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkerProfileQueryService {

    private final AdminClient adminClient;

    /* HR-064: 내 프로필 조회 */
    public EmployeeProfileResponse getProfile(Long employeeId) {
        return adminClient.getWorkerProfile(employeeId);
    }

    /* HR-065: 내 스킬 조회 */
    public List<EmployeeSkillResponse> getSkills(Long employeeId) {
        return adminClient.getWorkerSkills(employeeId);
    }

    /* HR-066: Tier 마일스톤 조회 */
    public List<TierMilestoneResponse> getTierMilestones(Long employeeId) {
        return adminClient.getTierMilestones(employeeId);
    }

    /* HR-067: Tier 차트 조회 */
    public List<TierChartPointResponse> getTierChart(Long employeeId) {
        return adminClient.getTierChart(employeeId);
    }
}
