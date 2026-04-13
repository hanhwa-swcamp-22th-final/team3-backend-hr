package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AlgorithmVersionSnapshotResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DepartmentDetailResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DomainKeywordRuleResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgEmployeeResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgTeamMembersResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.OrgUnitTreeResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TeamCreateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TeamMemberAddRequest;
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

    AlgorithmVersionSnapshotResponse getAlgorithmVersionSnapshot(Long algorithmVersionId);

    AlgorithmVersionSnapshotResponse getActiveAlgorithmVersion();

    OrgUnitTreeResponse getOrgTree();

    List<OrgEmployeeResponse> getEmployees(Long departmentId, Long teamId, String keyword, int page, int size);

    OrgTeamMembersResponse getTeamMembers(Long teamId);

    Long createDepartment(DepartmentCreateRequest request);

    Long updateDepartment(Long departmentId, DepartmentCreateRequest request);

    void deleteDepartment(Long departmentId);

    Long createTeam(Long departmentId, TeamCreateRequest request);

    Long updateTeam(Long teamId, TeamCreateRequest request);

    void deleteTeam(Long teamId);

    DepartmentDetailResponse getDepartmentDetail(Long departmentId);

    void addTeamMembers(Long teamId, TeamMemberAddRequest request);

    void removeTeamMember(Long teamId, Long employeeId);
}
