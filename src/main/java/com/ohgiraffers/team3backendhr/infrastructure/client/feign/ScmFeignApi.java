package com.ohgiraffers.team3backendhr.infrastructure.client.feign;

import com.ohgiraffers.team3backendhr.infrastructure.client.dto.ScmApiResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerTaskSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "scmFeignApi",
    url = "${feign.scm.url}",
    configuration = AdminFeignConfiguration.class
)
public interface ScmFeignApi {

    @GetMapping("/api/v1/scm/tasks/summary")
    ScmApiResponse<WorkerTaskSummaryResponse> getWorkerTaskSummary(@RequestParam Long employeeId);
}
