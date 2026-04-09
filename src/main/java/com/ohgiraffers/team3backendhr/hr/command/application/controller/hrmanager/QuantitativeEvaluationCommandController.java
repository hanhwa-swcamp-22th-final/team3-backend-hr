package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.service.QuantitativeEvaluationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr/hr-manager/quantitative-evaluations")
@RequiredArgsConstructor
public class QuantitativeEvaluationCommandController {

    private final QuantitativeEvaluationCommandService service;

    /** HRM 정량 평가 확정 */
    @PostMapping("/{evaluationId}/confirm")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> confirm(@PathVariable Long evaluationId) {
        service.confirm(evaluationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
