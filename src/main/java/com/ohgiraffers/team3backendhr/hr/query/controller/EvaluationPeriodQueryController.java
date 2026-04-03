package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.EvaluationPeriodDeadlineResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.EvaluationPeriodListResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.EvaluationPeriodQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr/evaluation-periods")
@RequiredArgsConstructor
public class EvaluationPeriodQueryController {

    private final EvaluationPeriodQueryService service;

    /* 평가 기간 목록 조회 — year·status 필터, 페이징 지원 */
    @GetMapping
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<ApiResponse<EvaluationPeriodListResponse>> getEvaluationPeriods(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getEvaluationPeriods(year, status, page, size)));
    }

    /* 마감일 조회 — 현재 IN_PROGRESS 기간의 endDate·daysRemaining 반환 */
    @GetMapping("/deadline")
    @PreAuthorize("hasAnyRole('TL', 'DL')")
    public ResponseEntity<ApiResponse<EvaluationPeriodDeadlineResponse>> getDeadline() {
        return ResponseEntity.ok(ApiResponse.success(service.getDeadline()));
    }
}
