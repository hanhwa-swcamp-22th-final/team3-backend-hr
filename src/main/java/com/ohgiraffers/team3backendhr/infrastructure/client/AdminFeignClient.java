package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AdminApiResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
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
}