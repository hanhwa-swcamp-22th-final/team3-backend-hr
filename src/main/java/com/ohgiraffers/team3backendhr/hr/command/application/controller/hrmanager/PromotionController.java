package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.hr.command.application.service.PromotionCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hr/promotions")
@RequiredArgsConstructor
public class PromotionCommandController {

    private final PromotionCommandService promotionCommandService;

    @PostMapping("/{candidateId}/confirm")
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<ApiResponse<Void>> confirmPromotion(@PathVariable Long candidateId) {
        promotionCommandService.confirmPromotion(candidateId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{candidateId}/hold")
    @PreAuthorize("hasRole('HRM')")
    public ResponseEntity<ApiResponse<Void>> suspendPromotion(@PathVariable Long candidateId) {
        promotionCommandService.suspendPromotion(candidateId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
