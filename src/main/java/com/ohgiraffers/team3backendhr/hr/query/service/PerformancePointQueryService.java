package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.PointHistoryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.PointSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.PerformancePointQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformancePointQueryService {

    private final PerformancePointQueryMapper performancePointQueryMapper;

    public PointSummaryResponse getPointSummary(Long employeeId) {
        return performancePointQueryMapper.findSummaryByEmployeeId(employeeId);
    }

    public List<PointHistoryResponse> getPointHistory(Long employeeId) {
        return performancePointQueryMapper.findHistoryByEmployeeId(employeeId);
    }
}
