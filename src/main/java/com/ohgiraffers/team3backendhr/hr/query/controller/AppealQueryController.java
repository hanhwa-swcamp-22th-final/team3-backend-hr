package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.AppealQueryService;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
public class AppealQueryController {

    private final AppealQueryService service;

    /* HRM·Worker — 이의신청 상세 조회 (Worker는 본인 것만) */
    @GetMapping("/appeals/{appealId}")
    @PreAuthorize("hasAnyAuthority('HRM', 'WORKER')")
    public ResponseEntity<ApiResponse<AppealDetailResponse>> getAppeal(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(service.getAppeal(appealId, userDetails)));
    }

    /* HRM — 이의신청 목록 조회 (상태 필터, 페이징) */
    @GetMapping("/appeals")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<AppealListResponse>> getAppeals(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getAppeals(status, page, size)));
    }

    /* Worker — 내 이의신청 목록 */
    @GetMapping("/appeals/me")
    @PreAuthorize("hasAuthority('WORKER')")
    public ResponseEntity<ApiResponse<List<AppealSummaryResponse>>> getMyAppeals(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(service.getMyAppeals(userDetails.getEmployeeId())));
    }


}
