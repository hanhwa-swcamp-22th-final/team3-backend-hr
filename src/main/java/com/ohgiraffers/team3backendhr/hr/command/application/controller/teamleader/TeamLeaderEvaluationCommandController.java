package com.ohgiraffers.team3backendhr.hr.command.application.controller.teamleader;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationUpdateRequest;
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

    @PatchMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('TL')")
    public ResponseEntity<ApiResponse<Void>> update(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @PathVariable Long employeeId,
            @RequestBody @Valid QualitativeEvaluationUpdateRequest request) {
        service.updateForTL(userDetails.getEmployeeId(), employeeId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
