package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.AntiGamingFlagItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.BiasReportItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationGradeSummaryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation.QuantitativeEvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation.QuantitativeEvaluationListResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.HrmEvaluationQueryService;
import com.ohgiraffers.team3backendhr.hr.query.service.QualitativeEvaluationQueryService;
import com.ohgiraffers.team3backendhr.hr.query.service.QuantitativeEvaluationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/evaluations")
@RequiredArgsConstructor
public class HrmEvaluationQueryController {

    private final QualitativeEvaluationQueryService qualService;
    private final HrmEvaluationQueryService hrmService;
    private final QuantitativeEvaluationQueryService quantService;

    /* HRM — 평가 목록 조회 (periodId·grade·status 필터, 페이징) */
    @GetMapping
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<EvaluationListResponse>> getEvaluations(
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                qualService.getEvaluations(periodId, grade, status, page, size)));
    }

    /* HRM — 평가 상세 조회 */
    @GetMapping("/{evalId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<EvaluationDetailResponse>> getEvaluation(
            @PathVariable Long evalId) {
        return ResponseEntity.ok(ApiResponse.success(qualService.getEvaluationDetail(evalId)));
    }

    /* HRM — 등급별 평가 집계 */
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<EvaluationGradeSummaryItem>>> getEvaluationSummary(
            @RequestParam(required = false) Long periodId) {
        return ResponseEntity.ok(ApiResponse.success(qualService.getEvaluationGradeSummary(periodId)));
    }

    /* HRM — 편향 보정 이력 */
    @GetMapping("/bias-report")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<BiasReportItem>>> getBiasReport() {
        return ResponseEntity.ok(ApiResponse.success(hrmService.getBiasReport()));
    }

    /* HRM — 어뷰징 감지 목록 */
    @GetMapping("/anti-gaming-flags")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<List<AntiGamingFlagItem>>> getAntiGamingFlags() {
        return ResponseEntity.ok(ApiResponse.success(hrmService.getAntiGamingFlags()));
    }

    /* HRM — 정량 평가 목록 조회 (periodId·status 필터, 페이징) */
    @GetMapping("/quantitative")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<QuantitativeEvaluationListResponse>> getQuantitativeList(
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                quantService.getList(periodId, status, page, size)));
    }

    /* HRM — 정량 평가 상세 조회 */
    @GetMapping("/quantitative/{evaluationId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<QuantitativeEvaluationDetailResponse>> getQuantitativeDetail(
            @PathVariable Long evaluationId) {
        return ResponseEntity.ok(ApiResponse.success(quantService.getDetail(evaluationId)));
    }
}
