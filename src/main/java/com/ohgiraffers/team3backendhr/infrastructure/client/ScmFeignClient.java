package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.infrastructure.client.dto.ScmApiResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerTaskSummaryResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.feign.ScmFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "feign.scm", name = "url")
public class ScmFeignClient implements ScmClient {

    private final ScmFeignApi scmFeignApi;

    @Override
    public WorkerTaskSummaryResponse getWorkerTaskSummary(Long employeeId) {
        ScmApiResponse<WorkerTaskSummaryResponse> response = scmFeignApi.getWorkerTaskSummary(employeeId);
        return response != null ? response.getData() : null;
    }
}
