package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.*;

import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Admin 서비스 HTTP 클라이언트.
 * application.yml 의 feign.admin.url 이 설정된 경우 생성된다.
 */
@Slf4j
@Component("adminRestClient")
@ConditionalOnProperty(prefix = "feign.admin", name = "url")
public class AdminRestClient implements AdminClient {

    private final RestTemplate restTemplate;
    private final String adminBaseUrl;

    public AdminRestClient(@Value("${feign.admin.url}") String adminBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.adminBaseUrl = adminBaseUrl;
    }

    @Override
    public List<WorkerResponse> getWorkers() {
        ResponseEntity<AdminApiResponse<List<WorkerResponse>>> response = restTemplate.exchange(
            adminBaseUrl + "/api/v1/organization/employees",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        AdminApiResponse<List<WorkerResponse>> body = response.getBody();
        return body != null ? body.getData() : List.of();
    }

    @Override
    public EmployeeProfileResponse getWorkerProfile(Long employeeId) {
        return restTemplate.getForObject(
                adminBaseUrl + "/api/v1/admin/employees/" + employeeId + "/profile",
                EmployeeProfileResponse.class
        );
    }

    @Override
    public List<EmployeeSkillResponse> getWorkerSkills(Long employeeId) {
        return restTemplate.exchange(
                adminBaseUrl + "/api/v1/admin/employees/" + employeeId + "/skills",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EmployeeSkillResponse>>() {}
        ).getBody();
    }

    @Override
    public List<TierMilestoneResponse> getTierMilestones(Long employeeId) {
        return restTemplate.exchange(
                adminBaseUrl + "/api/v1/admin/employees/" + employeeId + "/tier-milestones",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TierMilestoneResponse>>() {}
        ).getBody();
    }

    @Override
    public List<TierChartPointResponse> getTierChart(Long employeeId) {
        return restTemplate.exchange(
                adminBaseUrl + "/api/v1/admin/employees/" + employeeId + "/tier-chart",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TierChartPointResponse>>() {}
        ).getBody();
    }

    @Override
    public List<Long> getTeamMemberIds(Long leaderId) {
        return restTemplate.exchange(
                adminBaseUrl + "/api/v1/admin/employees/" + leaderId + "/team-members",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Long>>() {}
        ).getBody();
    }

    @Override
    public void updateEmployeeTier(Long employeeId, Grade newTier) {
        String url = adminBaseUrl + "/api/v1/admin/employees/" + employeeId + "/tier";
        RequestEntity<TierUpdateRequest> request = RequestEntity
                .patch(URI.create(url))
                .body(new TierUpdateRequest(newTier));
        restTemplate.exchange(request, Void.class);
        log.info("[AdminRestClient] 티어 업데이트 완료 - employeeId={}, newTier={}", employeeId, newTier);
    }

    @Override
    public OrgUnitTreeResponse getOrgTree() {
        return restTemplate.getForObject(
                adminBaseUrl + "/api/v1/admin/org/units",
                OrgUnitTreeResponse.class
        );
    }

    @Override
    public List<OrgEmployeeResponse> getEmployees(Long departmentId, Long teamId, String keyword, int page, int size) {
        String url = adminBaseUrl + "/api/v1/admin/org/employees?page=" + page + "&size=" + size
                + (departmentId != null ? "&departmentId=" + departmentId : "")
                + (teamId != null ? "&teamId=" + teamId : "")
                + (keyword != null ? "&keyword=" + keyword : "");
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<OrgEmployeeResponse>>() {}).getBody();
    }

    @Override
    public OrgTeamMembersResponse getTeamMembers(Long teamId) {
        return restTemplate.getForObject(
                adminBaseUrl + "/api/v1/admin/org/teams/" + teamId + "/members",
                OrgTeamMembersResponse.class
        );
    }

    @Override
    public Long createDepartment(DepartmentCreateRequest request) {
        return restTemplate.postForObject(
                adminBaseUrl + "/api/v1/admin/org/departments",
                request, Long.class
        );
    }

    @Override
    public Long updateDepartment(Long departmentId, DepartmentCreateRequest request) {
        restTemplate.put(adminBaseUrl + "/api/v1/admin/org/departments/" + departmentId, request);
        return departmentId;
    }

    @Override
    public void deleteDepartment(Long departmentId) {
        restTemplate.delete(adminBaseUrl + "/api/v1/admin/org/departments/" + departmentId);
    }

    @Override
    public Long createTeam(Long departmentId, TeamCreateRequest request) {
        return restTemplate.postForObject(
                adminBaseUrl + "/api/v1/admin/org/departments/" + departmentId + "/teams",
                request, Long.class
        );
    }

    @Override
    public Long updateTeam(Long teamId, TeamCreateRequest request) {
        restTemplate.put(adminBaseUrl + "/api/v1/admin/org/teams/" + teamId, request);
        return teamId;
    }

    @Override
    public void deleteTeam(Long teamId) {
        restTemplate.delete(adminBaseUrl + "/api/v1/admin/org/teams/" + teamId);
    }

    @Override
    public DepartmentDetailResponse getDepartmentDetail(Long departmentId) {
        return restTemplate.getForObject(
                adminBaseUrl + "/api/v1/admin/org/departments/" + departmentId,
                DepartmentDetailResponse.class
        );
    }

    @Override
    public void addTeamMembers(Long teamId, TeamMemberAddRequest request) {
        restTemplate.postForObject(
                adminBaseUrl + "/api/v1/admin/org/teams/" + teamId + "/members",
                request, Void.class
        );
    }

    @Override
    public void removeTeamMember(Long teamId, Long employeeId) {
        restTemplate.delete(adminBaseUrl + "/api/v1/admin/org/teams/" + teamId + "/members/" + employeeId);
    }
}
