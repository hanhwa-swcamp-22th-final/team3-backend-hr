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
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierUpdateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.feign.AdminFeignApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component("adminFeignClient")
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "feign.admin", name = "url")
public class AdminFeignClient implements AdminClient {

    private final AdminFeignApi adminFeignApi;

    @Override
    public List<WorkerResponse> getWorkers() {
        AdminApiResponse<List<WorkerResponse>> response = adminFeignApi.getWorkers();
        return response != null && response.getData() != null ? response.getData() : List.of();
    }

    @Override
    public EmployeeProfileResponse getWorkerProfile(Long employeeId) {
        AdminApiResponse<EmployeeProfileResponse> response = adminFeignApi.getWorkerProfile(employeeId);
        return response != null ? response.getData() : null;
    }

    @Override
    public List<EmployeeSkillResponse> getWorkerSkills(Long employeeId) {
        AdminApiResponse<List<EmployeeSkillResponse>> response = adminFeignApi.getWorkerSkills(employeeId);
        return response != null && response.getData() != null ? response.getData() : List.of();
    }

    @Override
    public List<TierChartPointResponse> getTierChart(Long employeeId) {
        AdminApiResponse<List<TierChartPointResponse>> response = adminFeignApi.getTierChart(employeeId);
        return response != null && response.getData() != null ? response.getData() : List.of();
    }

    @Override
    public List<Long> getTeamMemberIds(Long leaderId) {
        AdminApiResponse<List<Long>> response = adminFeignApi.getTeamMemberIds(leaderId);
        return response != null && response.getData() != null ? response.getData() : List.of();
    }

    @Override
    public void updateEmployeeTier(Long employeeId, Grade newTier) {
        adminFeignApi.updateEmployeeTier(employeeId, new TierUpdateRequest(newTier));
        log.info("[AdminFeignClient] Tier updated. employeeId={}, newTier={}", employeeId, newTier);
    }

    @Override
    public List<DomainKeywordRuleResponse> getActiveDomainKeywordRules() {
        AdminApiResponse<List<DomainKeywordRuleResponse>> response = adminFeignApi.getDomainKeywordRules();
        return response != null && response.getData() != null ? response.getData() : List.of();
    }

    @Override
    public AlgorithmVersionSnapshotResponse getAlgorithmVersionSnapshot(Long algorithmVersionId) {
        AdminApiResponse<AlgorithmVersionSnapshotResponse> response =
            adminFeignApi.getAlgorithmVersionDetail(algorithmVersionId);
        return response != null ? response.getData() : null;
    }

    @Override
    public OrgUnitTreeResponse getOrgTree() {
        AdminApiResponse<OrgUnitTreeResponse> response = adminFeignApi.getOrgTree();
        return response != null ? response.getData() : null;
    }

    @Override
    public List<OrgEmployeeResponse> getEmployees(Long departmentId, Long teamId, String keyword, int page, int size) {
        AdminApiResponse<List<OrgEmployeeResponse>> response =
            adminFeignApi.getEmployees(departmentId, teamId, keyword, page, size);
        return response != null && response.getData() != null ? response.getData() : List.of();
    }

    @Override
    public OrgTeamMembersResponse getTeamMembers(Long teamId) {
        AdminApiResponse<OrgTeamMembersResponse> response = adminFeignApi.getTeamMembers(teamId);
        return response != null ? response.getData() : null;
    }

    @Override
    public Long createDepartment(DepartmentCreateRequest request) {
        AdminApiResponse<Long> response = adminFeignApi.createDepartment(request);
        return response != null ? response.getData() : null;
    }

    @Override
    public Long updateDepartment(Long departmentId, DepartmentCreateRequest request) {
        adminFeignApi.updateDepartment(departmentId, request);
        return departmentId;
    }

    @Override
    public void deleteDepartment(Long departmentId) {
        adminFeignApi.deleteDepartment(departmentId);
    }

    @Override
    public Long createTeam(Long departmentId, TeamCreateRequest request) {
        AdminApiResponse<Long> response = adminFeignApi.createTeam(departmentId, request);
        return response != null ? response.getData() : null;
    }

    @Override
    public Long updateTeam(Long teamId, TeamCreateRequest request) {
        adminFeignApi.updateTeam(teamId, request);
        return teamId;
    }

    @Override
    public void deleteTeam(Long teamId) {
        adminFeignApi.deleteTeam(teamId);
    }

    @Override
    public DepartmentDetailResponse getDepartmentDetail(Long departmentId) {
        AdminApiResponse<DepartmentDetailResponse> response = adminFeignApi.getDepartmentDetail(departmentId);
        return response != null ? response.getData() : null;
    }

    @Override
    public void addTeamMembers(Long teamId, TeamMemberAddRequest request) {
        adminFeignApi.addTeamMembers(teamId, request);
    }

    @Override
    public void removeTeamMember(Long teamId, Long employeeId) {
        adminFeignApi.removeTeamMember(teamId, employeeId);
    }
}
