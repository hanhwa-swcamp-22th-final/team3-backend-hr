package com.ohgiraffers.team3backendhr.hr.command.application.controller.worker;

import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealRegisterRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.AppealCommandService;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/appeals")
@RequiredArgsConstructor
public class WorkerAppealCommandController {

    private final AppealCommandService service;

    @PostMapping
    @PreAuthorize("hasAuthority('WORKER')")
    public ResponseEntity<ApiResponse<Long>> register(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid AppealRegisterRequest request) {
        Long appealId = service.register(userDetails.getEmployeeId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success(appealId));
    }

    @PostMapping("/{appealId}/attachments")
    @PreAuthorize("hasAuthority('WORKER')")
    public ResponseEntity<ApiResponse<Void>> uploadAttachments(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestParam("files") List<MultipartFile> files) {
        service.uploadAttachments(appealId, userDetails.getEmployeeId(), files);
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    @PutMapping("/{appealId}")
    @PreAuthorize("hasAuthority('WORKER')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid AppealUpdateRequest request) {
        service.update(appealId, userDetails.getEmployeeId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{appealId}")
    @PreAuthorize("hasAuthority('WORKER')")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long appealId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        service.cancel(appealId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
