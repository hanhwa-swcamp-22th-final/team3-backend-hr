package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.TlEvaluationTargetResponse;
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
@RequestMapping("/api/v1/hr/team-leader/evaluations")
@RequiredArgsConstructor
public class TeamLeaderEvaluationQueryController {

    private final QualitativeEvaluationQueryService service;

    /* TL 팀원 평가 대상 조회 — 같은 부서 WORKER 목록 + 제출 상태 반환 */
    /* periodId 미전달 시 현재 IN_PROGRESS 기간으로 자동 resolve */
    @GetMapping("/targets")
    @PreAuthorize("hasAuthority('TL')")
    public ResponseEntity<ApiResponse<TlEvaluationTargetResponse>> getTargets(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestParam(required = false) Long periodId) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getTlTargets(userDetails.getEmployeeId(), periodId)));
    }

    /* TL 제출 완료 평가 상세 조회 — 본인이 제출한 것만 */
    @GetMapping("/{evalId}")
    @PreAuthorize("hasAuthority('TL')")
    public ResponseEntity<ApiResponse<EvaluationDetailResponse>> getEvaluationDetail(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @PathVariable Long evalId) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getTlEvaluationDetail(userDetails.getEmployeeId(), evalId)));
    }
}
