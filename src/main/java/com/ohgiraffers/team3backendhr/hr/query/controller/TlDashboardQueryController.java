package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.TlDashboardMemberItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.TlDashboardSummaryResponse;
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
@RequestMapping("/api/v1/hr/team-leader/dashboard")
@RequiredArgsConstructor
public class TlDashboardQueryController {

    private final DashboardQueryService dashboardQueryService;

    /* HR-005: 팀 지표 조회 */
    @GetMapping
    @PreAuthorize("hasAuthority('TL')")
    public ResponseEntity<ApiResponse<TlDashboardSummaryResponse>> getDashboard(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getTlDashboardSummary(userDetails.getEmployeeId())));
    }

    /* HR-006: 팀원 지표 조회 */
    @GetMapping("/members")
    @PreAuthorize("hasAuthority('TL')")
    public ResponseEntity<ApiResponse<List<TlDashboardMemberItem>>> getDashboardMembers(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardQueryService.getTlDashboardMembers(userDetails.getEmployeeId(), page, size)));
    }
}
