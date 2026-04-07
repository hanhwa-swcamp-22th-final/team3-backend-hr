package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.infrastructure.client.dto.ProductionStatsResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * ScmClient 임시 Mock 구현체.
 * SCM 서비스 API 확정 전까지 하드코딩 데이터를 반환한다.
 * scm.client.url 프로퍼티 설정 시 ScmRestClient 로 자동 교체됨.
 */
@Component
@ConditionalOnMissingBean(name = "scmRestClient")
public class ScmClientStub implements ScmClient {

    @Override
    public ProductionStatsResponse getProductionStats(Long employeeId, int year, int quarter) {
        return ProductionStatsResponse.builder()
                .employeeId(employeeId)
                .year(year)
                .quarter(quarter)
                .targetProduction(new BigDecimal("1000.00"))
                .actualProduction(new BigDecimal("950.00"))
                .defectRate(new BigDecimal("2.50"))
                .eIdx(new BigDecimal("0.95"))
                .build();
    }
}
