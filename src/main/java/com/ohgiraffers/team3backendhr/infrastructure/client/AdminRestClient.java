package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AdminApiResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DomainKeywordRuleResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierChartPointResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierMilestoneResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierUpdateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
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
        log.info("[AdminRestClient] Tier updated. employeeId={}, newTier={}", employeeId, newTier);
    }

    @Override
    public List<DomainKeywordRuleResponse> getActiveDomainKeywordRules() {
        ResponseEntity<AdminApiResponse<List<DomainKeywordRuleResponse>>> response = restTemplate.exchange(
            adminBaseUrl + "/api/v1/domain-keyword",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );
        AdminApiResponse<List<DomainKeywordRuleResponse>> body = response.getBody();
        return body != null && body.getData() != null ? body.getData() : List.of();
    }
}