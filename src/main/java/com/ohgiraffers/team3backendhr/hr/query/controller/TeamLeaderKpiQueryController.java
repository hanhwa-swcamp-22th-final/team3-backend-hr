package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.KpiMemberDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.KpiMemberSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.KpiQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/team-leader/kpi")
@RequiredArgsConstructor
public class TeamLeaderKpiQueryController {

    private final KpiQueryService kpiQueryService;

    /* HR-010: 팀원 정량 점수 산출 내역 조회 */
    @GetMapping
    @PreAuthorize("hasAuthority('TL')")
    public ResponseEntity<ApiResponse<List<KpiMemberSummaryResponse>>> getTeamKpi(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int quarter) {
        return ResponseEntity.ok(ApiResponse.success(
                kpiQueryService.getTeamKpiSummary(userDetails.getEmployeeId(), year, quarter)));
    }

    /* HR-011: 특정 팀원 정량 점수 산출 상세 조회 */
    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('TL')")
    public ResponseEntity<ApiResponse<KpiMemberDetailResponse>> getMemberKpi(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int quarter) {
        return ResponseEntity.ok(ApiResponse.success(
                kpiQueryService.getMemberKpiDetail(employeeId, year, quarter)));
    }
}
