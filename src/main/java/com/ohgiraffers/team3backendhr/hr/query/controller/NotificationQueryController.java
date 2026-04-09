package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NotificationResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NotificationSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/notifications")
@RequiredArgsConstructor
public class NotificationQueryController {

    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        List<NotificationResponse> list =
                notificationQueryService.getVisibleNotifications(userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<NotificationSummaryResponse>> getSummary(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        NotificationSummaryResponse summary =
                notificationQueryService.getSummary(userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
