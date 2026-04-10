package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.promotion;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PromotionStatusUpdateRequest {

    /** CONFIRMATION_OF_PROMOTION(확정) | SUSPENSION(보류) */
    @NotNull(message = "status는 필수입니다.")
    private final PromotionStatus status;
}
