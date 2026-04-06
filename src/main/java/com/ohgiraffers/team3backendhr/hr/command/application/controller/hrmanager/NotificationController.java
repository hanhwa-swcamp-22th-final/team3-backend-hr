package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hr/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationCommandService notificationCommandService;

    @PatchMapping("/{notificationId}/hide")
    public ResponseEntity<ApiResponse<Void>> hide(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        notificationCommandService.hide(notificationId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{notificationId}/ack")
    public ResponseEntity<ApiResponse<Void>> acknowledge(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        notificationCommandService.acknowledge(notificationId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
