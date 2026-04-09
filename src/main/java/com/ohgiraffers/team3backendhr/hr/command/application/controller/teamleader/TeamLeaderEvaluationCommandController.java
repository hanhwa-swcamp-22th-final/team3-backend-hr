package com.ohgiraffers.team3backendhr.hr.command.application.controller.teamleader;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.QualitativeEvaluationCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hr/team-leader/evaluations")
@RequiredArgsConstructor
public class TeamLeaderEvaluationCommandController {

    private final QualitativeEvaluationCommandService service;

    @PostMapping("/{employeeId}/draft")
    @PreAuthorize("hasAuthority('TL')")
    public ResponseEntity<ApiResponse<Void>> saveDraft(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @PathVariable Long employeeId,
            @RequestBody @Valid QualitativeEvaluationDraftRequest request) {
        service.saveDraft(userDetails.getEmployeeId(), employeeId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{employeeId}/submit")
    @PreAuthorize("hasAuthority('TL')")
    public ResponseEntity<ApiResponse<Void>> submit(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @PathVariable Long employeeId,
            @RequestBody @Valid QualitativeEvaluationSubmitRequest request) {
        service.submit(userDetails.getEmployeeId(), employeeId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
