package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
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
public class NoticeController {

    private final NoticeCommandService noticeCommandService;

    /* 즉시 게시 */
    @PostMapping
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> publishNotice(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid NoticePublishRequest request) {
        noticeCommandService.publishNotice(request, userDetails.getEmployeeId());
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    /* 예약 게시 */
    @PostMapping("/schedule")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> scheduleNotice(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody @Valid NoticeScheduleRequest request) {
        noticeCommandService.scheduleNotice(request, userDetails.getEmployeeId());
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    /* 임시 저장 — 생성된 noticeId 반환 (재저장 시 프론트에서 noticeId 포함하여 재요청) */
    @PostMapping("/draft")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Long>> draftNotice(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestBody NoticeDraftRequest request) {
        Long noticeId = noticeCommandService.draftNotice(request, userDetails.getEmployeeId());
        return ResponseEntity.status(201).body(ApiResponse.success(noticeId));
    }

    /* 수정 */
    @PutMapping("/{noticeId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeUpdateRequest request) {
        noticeCommandService.updateNotice(noticeId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* 삭제 */
    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long noticeId) {
        noticeCommandService.deleteNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
