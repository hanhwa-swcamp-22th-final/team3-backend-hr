package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;

import java.util.List;

public interface AdminClient {

    List<WorkerResponse> getWorkers();

    /* 승급 확정 시 Admin 서비스에 사원 현재 티어 갱신 요청 */
    void updateEmployeeTier(Long employeeId, Grade newTier);
}
