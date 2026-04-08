package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DomainKeywordRuleResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierChartPointResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierMilestoneResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import java.util.List;

public interface AdminClient {

    List<WorkerResponse> getWorkers();

    EmployeeProfileResponse getWorkerProfile(Long employeeId);

    List<EmployeeSkillResponse> getWorkerSkills(Long employeeId);

    List<TierMilestoneResponse> getTierMilestones(Long employeeId);

    List<TierChartPointResponse> getTierChart(Long employeeId);

    List<Long> getTeamMemberIds(Long leaderId);

    void updateEmployeeTier(Long employeeId, Grade newTier);

    List<DomainKeywordRuleResponse> getActiveDomainKeywordRules();
}