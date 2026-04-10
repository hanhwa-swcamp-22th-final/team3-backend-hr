package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.skillgap.SkillGapResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.SkillGapQueryService;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr/workers/me")
@RequiredArgsConstructor
public class SkillGapQueryController {

    private final SkillGapQueryService skillGapQueryService;

    /* HR-072: 스킬 갭 분석 및 AI 학습 추천 */
    @GetMapping("/skill-gap")
    @PreAuthorize("hasAuthority('WORKER')")
    public ResponseEntity<ApiResponse<SkillGapResponse>> getSkillGap(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestParam(required = false) String targetTier) {
        return ResponseEntity.ok(ApiResponse.success(
                skillGapQueryService.getSkillGap(userDetails.getEmployeeId(), targetTier)));
    }
}
