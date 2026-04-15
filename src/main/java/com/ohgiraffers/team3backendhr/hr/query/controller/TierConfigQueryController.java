package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.tierconfig.TierCriteriaItem;
import com.ohgiraffers.team3backendhr.hr.query.service.HrmEvaluationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/evaluation/criteria")
@RequiredArgsConstructor
public class TierConfigQueryController {

    private final HrmEvaluationQueryService queryService;

    /* HRM — 평가 기준 조회 (HR-EVAL-025) */
    @GetMapping
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<TierCriteriaItem>>> getCriteria() {
        return ResponseEntity.ok(ApiResponse.success(queryService.getCriteria()));
    }
}
