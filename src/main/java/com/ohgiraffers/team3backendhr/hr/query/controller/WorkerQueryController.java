package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.MissionResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.PointHistoryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.PointSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.MissionQueryService;
import com.ohgiraffers.team3backendhr.hr.query.service.PerformancePointQueryService;
import com.ohgiraffers.team3backendhr.hr.query.service.WorkerProfileQueryService;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeSkillResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierChartPointResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.TierMilestoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/workers/me")
@RequiredArgsConstructor
public class WorkerQueryController {

    private final PerformancePointQueryService performancePointQueryService;
    private final MissionQueryService missionQueryService;
    private final WorkerProfileQueryService workerProfileQueryService;

    @GetMapping("/point-summary")
    public ResponseEntity<ApiResponse<PointSummaryResponse>> getPointSummary(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        PointSummaryResponse summary =
                performancePointQueryService.getPointSummary(userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/point-history")
    public ResponseEntity<ApiResponse<List<PointHistoryResponse>>> getPointHistory(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        List<PointHistoryResponse> history =
                performancePointQueryService.getPointHistory(userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /* HR-071: 전체 미션 목록 조회 */
    @GetMapping("/missions")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getMissions(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                missionQueryService.getMissions(userDetails.getEmployeeId(), status, page, size)));
    }

    /* HR-070: 티어 달성 미션 조회 */
    @GetMapping("/missions/upgrade")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getUpgradeMissions(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                missionQueryService.getUpgradeMissions(userDetails.getEmployeeId())));
    }

    /* HR-064: 내 프로필 조회 */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> getProfile(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                workerProfileQueryService.getProfile(userDetails.getEmployeeId())));
    }

    /* HR-065: 내 스킬 조회 */
    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<EmployeeSkillResponse>>> getSkills(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                workerProfileQueryService.getSkills(userDetails.getEmployeeId())));
    }

    /* HR-066: Tier 마일스톤 조회 */
    @GetMapping("/tier-milestones")
    public ResponseEntity<ApiResponse<List<TierMilestoneResponse>>> getTierMilestones(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                workerProfileQueryService.getTierMilestones(userDetails.getEmployeeId())));
    }

    /* HR-067: Tier 차트 조회 */
    @GetMapping("/tier-chart")
    public ResponseEntity<ApiResponse<List<TierChartPointResponse>>> getTierChart(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                workerProfileQueryService.getTierChart(userDetails.getEmployeeId())));
    }
}
