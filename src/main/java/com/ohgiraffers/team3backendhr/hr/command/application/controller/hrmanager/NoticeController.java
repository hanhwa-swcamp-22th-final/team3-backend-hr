package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.NoticeCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hr/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeCommandService noticeCommandService;

    @PostMapping
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<ApiResponse<Void>> createNotice(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid NoticeCreateRequest request) {
        noticeCommandService.createNotice(request, userDetails.getEmployeeId());
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    @PutMapping("/{noticeId}")
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeUpdateRequest request) {
        noticeCommandService.updateNotice(noticeId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long noticeId) {
        noticeCommandService.deleteNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
