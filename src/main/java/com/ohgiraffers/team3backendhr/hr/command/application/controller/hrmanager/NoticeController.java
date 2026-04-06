package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticePublishRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeScheduleRequest;
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
public class NoticeCommandController {

    private final NoticeCommandService noticeCommandService;

    /* 즉시 게시 */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_HRM')")
    public ResponseEntity<ApiResponse<Void>> publishNotice(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid NoticePublishRequest request) {
        noticeCommandService.publishNotice(request, userDetails.getEmployeeId());
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    /* 예약 게시 */
    @PostMapping("/schedule")
    @PreAuthorize("hasAuthority('ROLE_HRM')")
    public ResponseEntity<ApiResponse<Void>> scheduleNotice(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid NoticeScheduleRequest request) {
        noticeCommandService.scheduleNotice(request, userDetails.getEmployeeId());
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    /* 임시 저장 */
    @PostMapping("/draft")
    @PreAuthorize("hasAuthority('ROLE_HRM')")
    public ResponseEntity<ApiResponse<Void>> draftNotice(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody NoticeDraftRequest request) {
        noticeCommandService.draftNotice(request, userDetails.getEmployeeId());
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    /* 수정 */
    @PutMapping("/{noticeId}")
    @PreAuthorize("hasAuthority('ROLE_HRM')")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeUpdateRequest request) {
        noticeCommandService.updateNotice(noticeId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* 삭제 */
    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasAuthority('ROLE_HRM')")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long noticeId) {
        noticeCommandService.deleteNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
