package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationTargetResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.QualitativeEvaluationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr/department-leader/evaluations")
@RequiredArgsConstructor
public class DepartmentLeaderEvaluationQueryController {

    private final QualitativeEvaluationQueryService service;

    /* DL 2차 평가 대상 조회 — level 1 SUBMITTED인 대상 + AI 추천 점수 반환 */
    /* periodId 미전달 시 현재 IN_PROGRESS 기간으로 자동 resolve */
    @GetMapping("/targets")
    @PreAuthorize("hasAuthority('DL')")
    public ResponseEntity<ApiResponse<DlEvaluationTargetResponse>> getTargets(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestParam(required = false) Long periodId) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getDlTargets(userDetails.getEmployeeId(), periodId)));
    }

    /* DL — 특정 사원의 1차 평가 항목 + AI 추천 점수 조회 (본인 부서 직원만) */
    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('DL')")
    public ResponseEntity<ApiResponse<DlEvaluationDetailResponse>> getEvaluationDetail(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @PathVariable Long employeeId,
            @RequestParam(required = false) Long periodId) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getDlEvaluationDetail(userDetails.getEmployeeId(), employeeId, periodId)));
    }
}
