package com.ohgiraffers.team3backendhr.appeal.query.controller;

import com.ohgiraffers.team3backendhr.appeal.query.dto.response.AppealListResponse;
import com.ohgiraffers.team3backendhr.appeal.query.dto.response.AppealSummaryResponse;
import com.ohgiraffers.team3backendhr.appeal.query.dto.response.ScoreModificationLogResponse;
import com.ohgiraffers.team3backendhr.appeal.query.service.AppealQueryService;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
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
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
public class AppealQueryController {

    private final AppealQueryService service;

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

    /* HRM — 점수 수정 이력 */
    @GetMapping("/score-modification-logs")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<ScoreModificationLogResponse>>> getScoreModificationLogs() {
        return ResponseEntity.ok(ApiResponse.success(service.getScoreModificationLogs()));
    }
}
