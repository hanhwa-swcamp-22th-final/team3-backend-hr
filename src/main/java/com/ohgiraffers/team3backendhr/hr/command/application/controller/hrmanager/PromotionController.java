package com.ohgiraffers.team3backendhr.hr.command.application.controller.hrmanager;

import com.ohgiraffers.team3backendhr.common.dto.ApiResponse;
import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.promotion.PromotionStatusUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.service.PromotionCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hr/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionCommandService promotionCommandService;

    @PatchMapping("/{candidateId}")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long candidateId,
            @RequestBody @Valid PromotionStatusUpdateRequest request) {
        if (request.getStatus() == PromotionStatus.CONFIRMATION_OF_PROMOTION) {
            promotionCommandService.confirmPromotion(candidateId);
        } else if (request.getStatus() == PromotionStatus.SUSPENSION) {
            promotionCommandService.suspendPromotion(candidateId);
        } else {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "유효하지 않은 status 값입니다.");
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/apply-tier")
    @PreAuthorize("hasAuthority('HRM')")
    public ResponseEntity<ApiResponse<Void>> applyTier() {
        promotionCommandService.applyTierForConfirmed();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
