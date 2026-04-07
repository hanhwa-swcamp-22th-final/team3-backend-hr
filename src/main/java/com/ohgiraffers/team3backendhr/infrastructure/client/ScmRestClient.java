package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.infrastructure.client.dto.ProductionStatsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * SCM 서비스 HTTP 클라이언트.
 * application.yml 에 feign.scm.url 이 설정된 경우 활성화된다.
 */
@Component("scmRestClient")
@ConditionalOnProperty(prefix = "feign.scm", name = "url")
public class ScmRestClient implements ScmClient {

    private final RestTemplate restTemplate;
    private final String scmBaseUrl;

    public ScmRestClient(@Value("${feign.scm.url}") String scmBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.scmBaseUrl = scmBaseUrl;
    }

    @Override
    public ProductionStatsResponse getProductionStats(Long employeeId, int year, int quarter) {
        String url = scmBaseUrl + "/api/v1/scm/stats/employees/" + employeeId
                + "?year=" + year + "&quarter=" + quarter;
        return restTemplate.getForObject(url, ProductionStatsResponse.class);
    }
}
