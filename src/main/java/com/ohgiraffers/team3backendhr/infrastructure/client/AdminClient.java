package com.ohgiraffers.team3backendhr.infrastructure.client;

import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;

import java.util.List;

public interface AdminClient {

    List<WorkerResponse> getWorkers();
}
