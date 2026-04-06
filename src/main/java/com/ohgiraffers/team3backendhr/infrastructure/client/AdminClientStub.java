package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AdminClient 임시 구현체.
 * admin.client.enabled=true 프로퍼티 설정 시 AdminRestClient 로 교체됨.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(name = "adminRestClient")
public class AdminClientStub implements AdminClient {

    @Override
    public List<WorkerResponse> getWorkers() {
        throw new UnsupportedOperationException("AdminClient 구현체가 아직 연결되지 않았습니다.");
    }

    @Override
    public void updateEmployeeTier(Long employeeId, Grade newTier) {
        log.warn("[AdminClientStub] updateEmployeeTier 호출 무시 — Admin 서비스 미연결 (employeeId={}, newTier={})",
                employeeId, newTier);
    }
}
