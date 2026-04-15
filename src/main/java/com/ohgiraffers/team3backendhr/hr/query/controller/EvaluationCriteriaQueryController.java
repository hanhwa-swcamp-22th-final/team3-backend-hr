package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.EvaluationCriteriaResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.tierconfig.TierCriteriaItem;
import com.ohgiraffers.team3backendhr.hr.query.service.HrmEvaluationQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr/evaluation/criteria")
@RequiredArgsConstructor
public class EvaluationCriteriaQueryController {

    private final HrmEvaluationQueryService queryService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('HRM','WORKER')")
    public ResponseEntity<ApiResponse<List<TierCriteriaItem>>> getTierCriteria() {
        return ResponseEntity.ok(ApiResponse.success(queryService.getTierCriteria()));
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<EvaluationCriteriaResponse>> getEvaluationCriteriaDetail() {
        return ResponseEntity.ok(ApiResponse.success(queryService.getEvaluationCriteriaDetail()));
    }
}
