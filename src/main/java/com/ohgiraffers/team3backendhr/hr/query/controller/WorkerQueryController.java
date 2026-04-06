package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.PointHistoryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.PointSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.PerformancePointQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/workers/me")
@RequiredArgsConstructor
public class WorkerQueryController {

    private final PerformancePointQueryService performancePointQueryService;

    @GetMapping("/point-summary")
    public ResponseEntity<ApiResponse<PointSummaryResponse>> getPointSummary(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        PointSummaryResponse summary =
                performancePointQueryService.getPointSummary(userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/point-history")
    public ResponseEntity<ApiResponse<List<PointHistoryResponse>>> getPointHistory(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        List<PointHistoryResponse> history =
                performancePointQueryService.getPointHistory(userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
