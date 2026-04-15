package com.ohgiraffers.team3backendhr.hr.command.application.controller.departmentleader;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealProcessRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.AppealCommandService;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr/department-leader/appeals")
@RequiredArgsConstructor
public class DepartmentLeaderAppealCommandController {

    private final AppealCommandService service;

    @PatchMapping("/{appealId}/approve")
    @PreAuthorize("hasAuthority('DL')")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid AppealProcessRequest request) {
        service.approveByDl(appealId, userDetails.getEmployeeId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{appealId}/reject")
    @PreAuthorize("hasAuthority('DL')")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid AppealProcessRequest request) {
        service.rejectByDl(appealId, userDetails.getEmployeeId(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
