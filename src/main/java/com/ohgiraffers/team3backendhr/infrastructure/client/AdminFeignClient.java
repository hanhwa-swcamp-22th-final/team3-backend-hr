package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
<<<<<<< feature/auth
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.*;
=======
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
>>>>>>> main
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
<<<<<<< feature/auth
    public OrgUnitTreeResponse getOrgTree() {
        return null;
=======
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
    public OrgUnitTreeResponse getOrgTree() {
        return adminRestClient.getOrgTree();
>>>>>>> main
    }

    @Override
    public List<OrgEmployeeResponse> getEmployees(Long departmentId, Long teamId, String keyword, int page, int size) {
<<<<<<< feature/auth
        return List.of();
=======
        return adminRestClient.getEmployees(departmentId, teamId, keyword, page, size);
>>>>>>> main
    }

    @Override
    public OrgTeamMembersResponse getTeamMembers(Long teamId) {
<<<<<<< feature/auth
        return null;
=======
        return adminRestClient.getTeamMembers(teamId);
>>>>>>> main
    }

    @Override
    public Long createDepartment(DepartmentCreateRequest request) {
<<<<<<< feature/auth
        return 0L;
=======
        return adminRestClient.createDepartment(request);
>>>>>>> main
    }

    @Override
    public Long updateDepartment(Long departmentId, DepartmentCreateRequest request) {
<<<<<<< feature/auth
        return 0L;
=======
        return adminRestClient.updateDepartment(departmentId, request);
>>>>>>> main
    }

    @Override
    public void deleteDepartment(Long departmentId) {
<<<<<<< feature/auth

=======
        adminRestClient.deleteDepartment(departmentId);
>>>>>>> main
    }

    @Override
    public Long createTeam(Long departmentId, TeamCreateRequest request) {
<<<<<<< feature/auth
        return 0L;
=======
        return adminRestClient.createTeam(departmentId, request);
>>>>>>> main
    }

    @Override
    public Long updateTeam(Long teamId, TeamCreateRequest request) {
<<<<<<< feature/auth
        return 0L;
=======
        return adminRestClient.updateTeam(teamId, request);
>>>>>>> main
    }

    @Override
    public void deleteTeam(Long teamId) {
<<<<<<< feature/auth

=======
        adminRestClient.deleteTeam(teamId);
>>>>>>> main
    }

    @Override
    public DepartmentDetailResponse getDepartmentDetail(Long departmentId) {
<<<<<<< feature/auth
        return null;
=======
        return adminRestClient.getDepartmentDetail(departmentId);
>>>>>>> main
    }

    @Override
    public void addTeamMembers(Long teamId, TeamMemberAddRequest request) {
<<<<<<< feature/auth

=======
        adminRestClient.addTeamMembers(teamId, request);
>>>>>>> main
    }

    @Override
    public void removeTeamMember(Long teamId, Long employeeId) {
<<<<<<< feature/auth

    }
}
=======
        adminRestClient.removeTeamMember(teamId, employeeId);
    }
}
>>>>>>> main
