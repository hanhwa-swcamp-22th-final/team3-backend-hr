package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.EvaluationPeriodCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hr/evaluation-periods")
@RequiredArgsConstructor
public class EvaluationPeriodCommandController {

    private final EvaluationPeriodCommandService service;

    @PostMapping
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> create(@RequestBody @Valid EvaluationPeriodCreateRequest request) {
        service.create(request);
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> close(@PathVariable Long id) {
        service.close(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> confirm(@PathVariable Long id) {
        service.confirm(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id,
                                                    @RequestBody EvaluationPeriodUpdateRequest request) {
        service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/snapshots/republish")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Integer>> republishSnapshots() {
        return ResponseEntity.ok(ApiResponse.success(service.republishSnapshots()));
    }
}