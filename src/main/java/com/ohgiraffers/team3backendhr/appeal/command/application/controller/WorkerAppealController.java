package com.ohgiraffers.team3backendhr.appeal.command.application.controller;

import com.ohgiraffers.team3backendhr.appeal.command.application.dto.request.AppealRegisterRequest;
import com.ohgiraffers.team3backendhr.appeal.command.application.dto.request.AppealUpdateRequest;
import com.ohgiraffers.team3backendhr.appeal.command.application.service.AppealService;
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
public class WorkerAppealController {

    private final AppealService service;

    @PostMapping
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<Void>> register(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid AppealRegisterRequest request) {
        service.register(userDetails.getEmployeeId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    @PutMapping("/{appealId}")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid AppealUpdateRequest request) {
        service.update(appealId, userDetails.getEmployeeId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{appealId}")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        service.cancel(appealId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
