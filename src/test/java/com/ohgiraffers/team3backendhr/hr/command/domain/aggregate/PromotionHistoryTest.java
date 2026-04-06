package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PromotionHistoryTest {

    private PromotionHistory underReview() {
        return PromotionHistory.builder()
                .tierPromotionId(1L)
                .employeeId(10L)
                .reviewerId(20L)
                .currentTierConfigId(1L)
                .targetTierConfigId(2L)
                .tierAccumulatedPoint(80)
                .build();
    }

    @Test
    @DisplayName("심사 중인 승급 후보를 확정하면 CONFIRMATION_OF_PROMOTION 상태가 된다")
    void confirm_underReview_changesStatusToConfirmed() {
        PromotionHistory history = underReview();

        history.confirm();

        assertThat(history.getTierPromoStatus()).isEqualTo(PromotionStatus.CONFIRMATION_OF_PROMOTION);
        assertThat(history.getTierReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("심사 중인 승급 후보를 보류하면 SUSPENSION 상태가 된다")
    void suspend_underReview_changesStatusToSuspension() {
        PromotionHistory history = underReview();

        history.suspend();

        assertThat(history.getTierPromoStatus()).isEqualTo(PromotionStatus.SUSPENSION);
        assertThat(history.getTierReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 확정된 승급 후보를 다시 확정하면 예외가 발생한다")
    void confirm_alreadyConfirmed_throwsException() {
        PromotionHistory history = underReview();
        history.confirm();

        assertThatThrownBy(history::confirm)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("심사 중인 승급 후보만 확정할 수 있습니다.");
    }

    @Test
    @DisplayName("이미 보류된 승급 후보를 다시 보류하면 예외가 발생한다")
    void suspend_alreadySuspended_throwsException() {
        PromotionHistory history = underReview();
        history.suspend();

        assertThatThrownBy(history::suspend)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("심사 중인 승급 후보만 보류할 수 있습니다.");
    }
}
