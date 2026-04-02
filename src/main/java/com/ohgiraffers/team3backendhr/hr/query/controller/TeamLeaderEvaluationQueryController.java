package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.QualitativeEvaluationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/hr/team-leader/evaluations")
@RequiredArgsConstructor
public class TeamLeaderEvaluationQueryController {

    private final QualitativeEvaluationQueryService service;

    /* TL 팀원 평가 대상 조회 — 같은 부서 WORKER 목록 + 제출 상태 반환 */
    /* periodId 미전달 시 현재 IN_PROGRESS 기간으로 자동 resolve */
    @GetMapping("/targets")
    @PreAuthorize("hasRole('TL')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTargets(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestParam(required = false) Long periodId) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getTlTargets(userDetails.getEmployeeId(), periodId)));
    }
}
