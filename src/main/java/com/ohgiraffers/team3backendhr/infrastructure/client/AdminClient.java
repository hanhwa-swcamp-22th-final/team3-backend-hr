package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierChartPointResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierMilestoneResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;

import java.util.List;

public interface AdminClient {

    List<WorkerResponse> getWorkers();

    /* HR-064: 사원 프로필 조회 */
    EmployeeProfileResponse getWorkerProfile(Long employeeId);

    /* HR-065: 사원 보유 스킬 조회 */
    List<EmployeeSkillResponse> getWorkerSkills(Long employeeId);

    /* HR-066: 티어 마일스톤 조회 */
    List<TierMilestoneResponse> getTierMilestones(Long employeeId);

    /* HR-067: 티어 차트(이력) 조회 */
    List<TierChartPointResponse> getTierChart(Long employeeId);

    /* TL KPI용: 팀장의 팀원 ID 목록 조회 */
    List<Long> getTeamMemberIds(Long leaderId);

    /* HR-063: 승급 확정 시 Admin 서비스에 사원 현재 티어 갱신 요청 */
    void updateEmployeeTier(Long employeeId, Grade newTier);
}
