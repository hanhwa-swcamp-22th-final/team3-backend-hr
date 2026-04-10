package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.PromotionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr/promotions")
@RequiredArgsConstructor
public class PromotionQueryController {

    private final PromotionQueryService service;

    /* HRM — 승급 심사 요약 */
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<PromotionSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(service.getSummary()));
    }

    /* HRM — 승급 후보 목록 (targetTier 필터, 페이징) */
    @GetMapping("/candidates")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<PromotionCandidateListResponse>> getCandidates(
            @RequestParam(required = false) String targetTier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getCandidates(targetTier, page, size)));
    }

    /* HRM — 승급 후보 상세 */
    @GetMapping("/{candidateId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<PromotionCandidateDetailResponse>> getCandidateDetail(
            @PathVariable Long candidateId) {
        return ResponseEntity.ok(ApiResponse.success(service.getCandidateDetail(candidateId)));
    }
}
