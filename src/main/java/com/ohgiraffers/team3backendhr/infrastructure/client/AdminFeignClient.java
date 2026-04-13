package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AdminApiResponse;
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
import com.ohgiraffers.team3backendhr.infrastructure.client.feign.AdminFeignApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("adminFeignClient")
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "feign.admin", name = "url")
public class AdminFeignClient implements AdminClient {

    private final AdminFeignApi adminFeignApi;
    private final AdminRestClient adminRestClient;

    @Override
    public List<WorkerResponse> getWorkers() {
        AdminApiResponse<List<WorkerResponse>> response = adminFeignApi.getWorkers();
        if (response == null || response.getData() == null) {
            return List.of();
        }
        return response.getData();
    }

    @Override
    public EmployeeProfileResponse getWorkerProfile(Long employeeId) {
        return adminRestClient.getWorkerProfile(employeeId);
    }

    @Override
    public List<EmployeeSkillResponse> getWorkerSkills(Long employeeId) {
        return adminRestClient.getWorkerSkills(employeeId);
    }

    @Override
    public List<TierMilestoneResponse> getTierMilestones(Long employeeId) {
        return adminRestClient.getTierMilestones(employeeId);
    }

    @Override
    public List<TierChartPointResponse> getTierChart(Long employeeId) {
        return adminRestClient.getTierChart(employeeId);
    }

    @Override
    public List<Long> getTeamMemberIds(Long leaderId) {
        return adminRestClient.getTeamMemberIds(leaderId);
    }

    @Override
    public void updateEmployeeTier(Long employeeId, Grade newTier) {
        adminRestClient.updateEmployeeTier(employeeId, newTier);
    }

    @Override
    public List<DomainKeywordRuleResponse> getActiveDomainKeywordRules() {
        AdminApiResponse<List<DomainKeywordRuleResponse>> response = adminFeignApi.getDomainKeywordRules();
        if (response == null || response.getData() == null) {
            return List.of();
        }
        return response.getData();
    }

    @Override
    public AlgorithmVersionSnapshotResponse getAlgorithmVersionSnapshot(Long algorithmVersionId) {
        AdminApiResponse<AlgorithmVersionSnapshotResponse> response =
            adminFeignApi.getAlgorithmVersionDetail(algorithmVersionId);
        return response == null ? null : response.getData();
    }

    @Override
    public AlgorithmVersionSnapshotResponse getActiveAlgorithmVersion() {
        AdminApiResponse<List<AlgorithmVersionSnapshotResponse>> response =
            adminFeignApi.getAlgorithmVersionList(true);
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            return null;
        }
        return response.getData().get(0);
    }

    @Override
    public OrgUnitTreeResponse getOrgTree() {
        return adminRestClient.getOrgTree();
    }

    @Override
    public List<OrgEmployeeResponse> getEmployees(Long departmentId, Long teamId, String keyword, int page, int size) {
        return adminRestClient.getEmployees(departmentId, teamId, keyword, page, size);
    }

    @Override
    public OrgTeamMembersResponse getTeamMembers(Long teamId) {
        return adminRestClient.getTeamMembers(teamId);
    }

    @Override
    public Long createDepartment(DepartmentCreateRequest request) {
        return adminRestClient.createDepartment(request);
    }

    @Override
    public Long updateDepartment(Long departmentId, DepartmentCreateRequest request) {
        return adminRestClient.updateDepartment(departmentId, request);
    }

    @Override
    public void deleteDepartment(Long departmentId) {
        adminRestClient.deleteDepartment(departmentId);
    }

    @Override
    public Long createTeam(Long departmentId, TeamCreateRequest request) {
        return adminRestClient.createTeam(departmentId, request);
    }

    @Override
    public Long updateTeam(Long teamId, TeamCreateRequest request) {
        return adminRestClient.updateTeam(teamId, request);
    }

    @Override
    public void deleteTeam(Long teamId) {
        adminRestClient.deleteTeam(teamId);
    }

    @Override
    public DepartmentDetailResponse getDepartmentDetail(Long departmentId) {
        return adminRestClient.getDepartmentDetail(departmentId);
    }

    @Override
    public void addTeamMembers(Long teamId, TeamMemberAddRequest request) {
        adminRestClient.addTeamMembers(teamId, request);
    }

    @Override
    public void removeTeamMember(Long teamId, Long employeeId) {
        adminRestClient.removeTeamMember(teamId, employeeId);
    }
}
