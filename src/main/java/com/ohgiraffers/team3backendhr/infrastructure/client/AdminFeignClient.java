package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.*;
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
    public OrgUnitTreeResponse getOrgTree() {
        return null;
    }

    @Override
    public List<OrgEmployeeResponse> getEmployees(Long departmentId, Long teamId, String keyword, int page, int size) {
        return List.of();
    }

    @Override
    public OrgTeamMembersResponse getTeamMembers(Long teamId) {
        return null;
    }

    @Override
    public Long createDepartment(DepartmentCreateRequest request) {
        return 0L;
    }

    @Override
    public Long updateDepartment(Long departmentId, DepartmentCreateRequest request) {
        return 0L;
    }

    @Override
    public void deleteDepartment(Long departmentId) {

    }

    @Override
    public Long createTeam(Long departmentId, TeamCreateRequest request) {
        return 0L;
    }

    @Override
    public Long updateTeam(Long teamId, TeamCreateRequest request) {
        return 0L;
    }

    @Override
    public void deleteTeam(Long teamId) {

    }

    @Override
    public DepartmentDetailResponse getDepartmentDetail(Long departmentId) {
        return null;
    }

    @Override
    public void addTeamMembers(Long teamId, TeamMemberAddRequest request) {

    }

    @Override
    public void removeTeamMember(Long teamId, Long employeeId) {

    }
}