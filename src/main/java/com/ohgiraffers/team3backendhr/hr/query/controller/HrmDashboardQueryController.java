package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiDetailItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiTrendItem;
import com.ohgiraffers.team3backendhr.hr.query.service.DashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/kpi")
@RequiredArgsConstructor
public class HrmDashboardQueryController {

    private final DashboardQueryService dashboardQueryService;

    /* HR-001: KPI 요약 조회 */
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<HrmKpiSummaryResponse>> getKpiSummary(
            @RequestParam int year,
            @RequestParam int quarter) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getHrmKpiSummary(year, quarter)));
    }

    /* HR-002: KPI 상세 조회 */
    @GetMapping("/details")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<HrmKpiDetailItem>>> getKpiDetails(
            @RequestParam int year,
            @RequestParam int quarter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getHrmKpiDetails(year, quarter, page, size)));
    }

    /* HR-003: KPI 추이 조회 */
    @GetMapping("/trends")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<HrmKpiTrendItem>>> getKpiTrends(
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getHrmKpiTrends(year)));
    }
}
