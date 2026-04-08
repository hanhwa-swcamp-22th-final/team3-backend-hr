package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.infrastructure.client.dto.ProductionStatsResponse;

public interface ScmClient {

    /* TL KPI용: 사원의 연도/분기별 생산 실적 조회 */
    ProductionStatsResponse getProductionStats(Long employeeId, int year, int quarter);
}
