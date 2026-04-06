package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealReviewRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.AppealCommandService;
import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hr/appeals")
@RequiredArgsConstructor
public class HrmAppealCommandController {

    private final AppealCommandService service;

    @PostMapping("/{appealId}/approve")
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid AppealReviewRequest request) {
        service.approve(appealId, userDetails.getEmployeeId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{appealId}/reject")
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        service.reject(appealId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{appealId}/hold")
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<ApiResponse<Void>> hold(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        service.hold(appealId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
