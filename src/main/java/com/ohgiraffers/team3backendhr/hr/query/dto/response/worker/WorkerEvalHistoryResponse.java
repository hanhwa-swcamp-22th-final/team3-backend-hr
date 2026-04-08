package com.ohgiraffers.team3backendhr.hr.query.dto.response.worker;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WorkerEvalHistoryResponse {

    private List<WorkerEvalHistoryItem> content;
    private long totalCount;
}
