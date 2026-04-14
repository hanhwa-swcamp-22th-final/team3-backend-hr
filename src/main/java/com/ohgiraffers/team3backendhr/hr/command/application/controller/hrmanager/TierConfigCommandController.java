package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.EvaluationCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.TierCriteriaCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr/evaluation/criteria")
@RequiredArgsConstructor
public class TierConfigCommandController {

    private final TierCriteriaCommandService commandService;

    @PostMapping
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> createCriteria(
            @RequestBody @Valid EvaluationCriteriaSaveRequest request) {
        commandService.createCriteria(request);
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> updateCriteria(
            @RequestBody @Valid EvaluationCriteriaSaveRequest request) {
        commandService.updateCriteria(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> deleteCriteria() {
        commandService.deleteCriteria();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
