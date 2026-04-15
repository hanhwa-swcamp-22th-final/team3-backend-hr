package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.EvaluationCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.response.criteria.EvaluationCriteriaCommandResponse;
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
    public ResponseEntity<ApiResponse<EvaluationCriteriaCommandResponse>> createCriteria(
            @RequestBody @Valid EvaluationCriteriaSaveRequest request) {
        EvaluationCriteriaCommandResponse response = commandService.createCriteria(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<EvaluationCriteriaCommandResponse>> updateCriteria(
            @RequestBody @Valid EvaluationCriteriaSaveRequest request) {
        EvaluationCriteriaCommandResponse response = commandService.updateCriteria(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> deleteCriteria() {
        commandService.deleteCriteria();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
