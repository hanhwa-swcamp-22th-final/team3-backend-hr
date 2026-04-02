package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AdminClient 임시 구현체.
 * 추후 RestTemplate 또는 Feign 기반 실제 구현체로 교체 예정.
 */
@Component
public class AdminClientStub implements AdminClient {

    @Override
    public List<WorkerResponse> getWorkers() {
        throw new UnsupportedOperationException("AdminClient 구현체가 아직 연결되지 않았습니다.");
    }
}
