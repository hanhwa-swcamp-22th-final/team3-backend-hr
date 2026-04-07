package com.ohgiraffers.team3backendhr.hr.query.controller;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.service.NoticeCommandService;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticePinnedResponse;
import com.ohgiraffers.team3backendhr.hr.query.service.NoticeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/notices")
@RequiredArgsConstructor
public class NoticeQueryController {

    private final NoticeQueryService noticeQueryService;
    private final NoticeCommandService noticeCommandService;

    /**
     * HR-012: 공지 목록 조회
     * - HRM: status 파라미터로 게시중·예약·임시 모두 조회 가능
     * - Worker/TL/DL: notice_status = POSTING 강제, isImportant 필터만 허용
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeListResponse>>> getNotices(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isImportant,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        boolean isHrm = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("HRM"));

        String effectiveStatus = isHrm ? status : "POSTING";

        return ResponseEntity.ok(ApiResponse.success(
                noticeQueryService.getNotices(keyword, isImportant, effectiveStatus, page, size)));
    }

    /**
     * HR-017: 상단 고정 공지 단건 조회
     * is_important = 1 AND POSTING AND importantEndAt > NOW() 조건 중 최신 1건
     */
    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<NoticePinnedResponse>> getPinnedNotice() {
        return ResponseEntity.ok(ApiResponse.success(noticeQueryService.getPinnedNotice()));
    }

    /**
     * HR-013: 공지 상세 조회 (조회수 증가 포함)
     */
    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNoticeDetail(
            @PathVariable Long noticeId) {
        noticeCommandService.incrementViews(noticeId);
        return ResponseEntity.ok(ApiResponse.success(noticeQueryService.getNoticeDetail(noticeId)));
    }
}
