package com.ohgiraffers.team3backendhr.infrastructure.client.feign;

import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AdminApiResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AlgorithmVersionSnapshotResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DomainKeywordRuleResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "adminFeignApi",
    url = "${feign.admin.url}",
    configuration = AdminFeignConfiguration.class
)
public interface AdminFeignApi {

    @GetMapping("/api/v1/organization/employees")
    AdminApiResponse<List<WorkerResponse>> getWorkers();

    @GetMapping("/api/v1/domain-keyword")
    AdminApiResponse<List<DomainKeywordRuleResponse>> getDomainKeywordRules();

    @GetMapping("/api/v1/algorithm-version/{algorithmVersionId}")
    AdminApiResponse<AlgorithmVersionSnapshotResponse> getAlgorithmVersionDetail(
        @PathVariable Long algorithmVersionId
    );
}
