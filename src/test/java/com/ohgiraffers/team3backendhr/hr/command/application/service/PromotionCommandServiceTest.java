package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.PromotionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.TierConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PromotionHistoryRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PromotionCommandServiceTest {

    @Mock
    private PromotionHistoryRepository promotionHistoryRepository;

    @Mock
    private TierConfigRepository tierConfigRepository;

    @Mock
    private AdminClient adminClient;

    @InjectMocks
    private PromotionCommandService promotionCommandService;

    private PromotionHistory buildHistory(PromotionStatus status) {
        return PromotionHistory.builder()
                .tierPromotionId(1000L)
                .employeeId(99L)
                .reviewerId(1L)
                .currentTierConfigId(10L)
                .targetTierConfigId(20L)
                .tierPromoStatus(status)
                .build();
    }

    private TierConfig buildTierConfig(Long id, Grade grade) {
        return TierConfig.builder()
                .tierConfigId(id)
                .tierConfigTier(grade)
                .build();
    }

    @Nested
    @DisplayName("confirmPromotion 메서드")
    class ConfirmPromotion {

        @Test
        @DisplayName("심사 중인 승급 후보가 확정되고 Admin 서비스에 티어 갱신을 요청한다")
        void confirmPromotion_changesStatusAndUpdatesAdminTier() {
            // given
            PromotionHistory history = buildHistory(PromotionStatus.UNDER_REVIEW);
            given(promotionHistoryRepository.findById(1000L)).willReturn(Optional.of(history));
            given(tierConfigRepository.findById(20L)).willReturn(Optional.of(buildTierConfig(20L, Grade.A)));

            // when
            promotionCommandService.confirmPromotion(1000L);

            // then
            assertThat(history.getTierPromoStatus()).isEqualTo(PromotionStatus.CONFIRMATION_OF_PROMOTION);
            assertThat(history.getTierReviewedAt()).isNotNull();
            verify(adminClient).updateEmployeeTier(99L, Grade.A);
        }

        @Test
        @DisplayName("존재하지 않는 승급 이력 확정 시 예외가 발생한다")
        void confirmPromotion_notFound_throwsException() {
            // given
            given(promotionHistoryRepository.findById(9999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> promotionCommandService.confirmPromotion(9999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("승급 이력을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("이미 확정된 승급 후보를 다시 확정하면 예외가 발생한다")
        void confirmPromotion_alreadyConfirmed_throwsException() {
            // given
            PromotionHistory history = buildHistory(PromotionStatus.CONFIRMATION_OF_PROMOTION);
            given(promotionHistoryRepository.findById(1000L)).willReturn(Optional.of(history));

            // when & then
            assertThatThrownBy(() -> promotionCommandService.confirmPromotion(1000L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("심사 중인 승급 후보만 확정할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("suspendPromotion 메서드")
    class SuspendPromotion {

        @Test
        @DisplayName("심사 중인 승급 후보가 보류된다")
        void suspendPromotion_changesStatusToSuspension() {
            // given
            PromotionHistory history = buildHistory(PromotionStatus.UNDER_REVIEW);
            given(promotionHistoryRepository.findById(1000L)).willReturn(Optional.of(history));

            // when
            promotionCommandService.suspendPromotion(1000L);

            // then
            assertThat(history.getTierPromoStatus()).isEqualTo(PromotionStatus.SUSPENSION);
            assertThat(history.getTierReviewedAt()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 승급 이력 보류 시 예외가 발생한다")
        void suspendPromotion_notFound_throwsException() {
            // given
            given(promotionHistoryRepository.findById(9999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> promotionCommandService.suspendPromotion(9999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("승급 이력을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("이미 보류된 승급 후보를 다시 보류하면 예외가 발생한다")
        void suspendPromotion_alreadySuspended_throwsException() {
            // given
            PromotionHistory history = buildHistory(PromotionStatus.SUSPENSION);
            given(promotionHistoryRepository.findById(1000L)).willReturn(Optional.of(history));

            // when & then
            assertThatThrownBy(() -> promotionCommandService.suspendPromotion(1000L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("심사 중인 승급 후보만 보류할 수 있습니다.");
        }
    }
}
