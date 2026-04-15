package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.TierCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.TierCriteriaCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/evaluation/criteria")
@RequiredArgsConstructor
public class TierConfigCommandController {

    private final TierCriteriaCommandService commandService;

    /* HRM — 평가 기준 저장 (HR-EVAL-026) */
    @PutMapping
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> saveCriteria(
            @RequestBody @Valid List<TierCriteriaSaveRequest> requests) {
        commandService.saveCriteria(requests);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
