package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.service.PromotionCommandService;
import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hr/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionCommandService promotionCommandService;

    @PostMapping("/{candidateId}/confirm")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> confirmPromotion(
        @PathVariable Long candidateId,
        @AuthenticationPrincipal EmployeeUserDetails userDetails
    ) {
        promotionCommandService.confirmPromotion(candidateId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/apply-tier")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> applyTier() {
        promotionCommandService.applyTierForConfirmed();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{candidateId}/hold")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> suspendPromotion(
        @PathVariable Long candidateId,
        @AuthenticationPrincipal EmployeeUserDetails userDetails
    ) {
        promotionCommandService.suspendPromotion(candidateId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
