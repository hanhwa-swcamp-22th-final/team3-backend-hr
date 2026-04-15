package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerTaskSummaryResponse;

public interface ScmClient {

    WorkerTaskSummaryResponse getWorkerTaskSummary(Long employeeId);
}
