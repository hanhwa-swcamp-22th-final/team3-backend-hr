package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationConfirmRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.QualitativeEvaluationCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hr/hr-manager/evaluations")
@RequiredArgsConstructor
public class HrmEvaluationCommandController {

    private final QualitativeEvaluationCommandService service;

    @PostMapping("/{employeeId}/confirm")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> confirmFinal(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @PathVariable Long employeeId,
            @RequestBody @Valid QualitativeEvaluationConfirmRequest request) {
        service.confirmFinal(userDetails.getEmployeeId(), employeeId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
