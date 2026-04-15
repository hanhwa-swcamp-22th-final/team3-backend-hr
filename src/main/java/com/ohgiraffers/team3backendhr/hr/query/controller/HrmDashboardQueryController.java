package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiDetailItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiTrendItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmTeamStatsItem;
import com.ohgiraffers.team3backendhr.hr.query.service.DashboardQueryService;
import com.ohgiraffers.team3backendhr.hr.query.service.KpiReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/kpi")
@RequiredArgsConstructor
public class HrmDashboardQueryController {

    private final DashboardQueryService dashboardQueryService;
    private final KpiReportService kpiReportService;

    /* HR-001: KPI 요약 조회 */
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<HrmKpiSummaryResponse>> getKpiSummary(
            @RequestParam int year,
            @RequestParam int evalSequence) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getHrmKpiSummary(year, evalSequence)));
    }

    /* HR-002: KPI 상세 조회 */
    @GetMapping("/details")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<HrmKpiDetailItem>>> getKpiDetails(
            @RequestParam int year,
            @RequestParam int evalSequence,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getHrmKpiDetails(year, evalSequence, page, size)));
    }

    /* HR-003: KPI 추이 조회 */
    @GetMapping("/trends")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<HrmKpiTrendItem>>> getKpiTrends(
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getHrmKpiTrends(year)));
    }

    @GetMapping("/team-stats")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<HrmTeamStatsItem>>> getTeamStats(
            @RequestParam int year,
            @RequestParam int evalSequence) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getHrmTeamStats(year, evalSequence)));
    }

    /**
     * HR-004: KPI 보고서 다운로드 (엑셀)
     * 개선: 파일명 한글 깨짐 방지 (RFC 5987 인코딩 적용)
     */
    @GetMapping("/report/download")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<byte[]> downloadKpiReport(
            @RequestParam int year,
            @RequestParam int evalSequence) {
        
        byte[] excelBytes = kpiReportService.generateHrmKpiExcel(year, evalSequence);
        
        String fileName = "전사_KPI_보고서_" + year + "_Q" + evalSequence + ".xlsx";
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
