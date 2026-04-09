package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardMemberItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.DlDashboardTeamItem;
import com.ohgiraffers.team3backendhr.hr.query.service.DashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/department-leader")
@RequiredArgsConstructor
public class DlDashboardQueryController {

    private final DashboardQueryService dashboardQueryService;

    /* HR-007: 부서 지표 조회 */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('DL')")
    public ResponseEntity<ApiResponse<DlDashboardSummaryResponse>> getDashboard(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getDlDashboardSummary(userDetails.getEmployeeId())));
    }

    /* HR-008: 팀별 상태 조회 */
    @GetMapping("/dashboard/teams")
    @PreAuthorize("hasAuthority('DL')")
    public ResponseEntity<ApiResponse<List<DlDashboardTeamItem>>> getDashboardTeams(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getDlDashboardTeams(userDetails.getEmployeeId())));
    }

    /* HR-009: 팀원 역량/성과 목록 조회 */
    @GetMapping("/members")
    @PreAuthorize("hasAuthority('DL')")
    public ResponseEntity<ApiResponse<List<DlDashboardMemberItem>>> getMembers(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getDlDashboardMembers(userDetails.getEmployeeId(), page, size)));
    }
}
