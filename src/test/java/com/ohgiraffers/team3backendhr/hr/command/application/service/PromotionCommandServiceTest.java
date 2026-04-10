package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.PromotionHistoryRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.PromotionEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PromotionCommandServiceTest {

    @Mock
    private PromotionHistoryRepository promotionHistoryRepository;

    @Mock
    private TierConfigRepository tierConfigRepository;

    @Mock
    private AdminClient adminClient;

    @Mock
    private PromotionEventPublisher promotionEventPublisher;

    @InjectMocks
    private PromotionCommandService promotionCommandService;

    private PromotionHistory buildHistory(PromotionStatus status) {
        return PromotionHistory.builder()
                .tierPromotionId(1000L)
                .employeeId(99L)
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
        @DisplayName("심사 중인 승급 후보가 CONFIRMATION_OF_PROMOTION 상태로 변경된다 (Admin 호출 없음)")
        void confirmPromotion_changesStatusToConfirmed() {
            // given
            PromotionHistory history = buildHistory(PromotionStatus.UNDER_REVIEW);
            given(promotionHistoryRepository.findById(1000L)).willReturn(Optional.of(history));

            // when
            promotionCommandService.confirmPromotion(1000L, 77L);

            // then
            assertThat(history.getTierPromoStatus()).isEqualTo(PromotionStatus.CONFIRMATION_OF_PROMOTION);
            assertThat(history.getTierReviewedAt()).isNotNull();
            assertThat(history.getReviewerId()).isEqualTo(77L);
            verifyNoInteractions(adminClient);
        }

        @Test
        @DisplayName("존재하지 않는 승급 이력 확정 시 예외가 발생한다")
        void confirmPromotion_notFound_throwsException() {
            // given
            given(promotionHistoryRepository.findById(9999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> promotionCommandService.confirmPromotion(9999L, 77L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("승급 이력을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("이미 확정된 승급 후보를 다시 확정하면 예외가 발생한다")
        void confirmPromotion_alreadyConfirmed_throwsException() {
            // given
            PromotionHistory history = buildHistory(PromotionStatus.CONFIRMATION_OF_PROMOTION);
            given(promotionHistoryRepository.findById(1000L)).willReturn(Optional.of(history));

            // when & then
            assertThatThrownBy(() -> promotionCommandService.confirmPromotion(1000L, 77L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("심사 중인 승급 후보만 처리할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("applyTierForConfirmed 메서드")
    class ApplyTierForConfirmed {

        @Test
        @DisplayName("CONFIRMATION_OF_PROMOTION 상태인 이력들의 티어가 Admin에 반영되고 TIER_APPLIED 로 전이된다")
        void applyTierForConfirmed_appliesAndTransitionsAll() {
            // given
            PromotionHistory h1 = buildHistory(PromotionStatus.CONFIRMATION_OF_PROMOTION);
            PromotionHistory h2 = PromotionHistory.builder()
                    .tierPromotionId(2000L).employeeId(88L).reviewerId(1L)
                    .currentTierConfigId(10L).targetTierConfigId(30L)
                    .tierPromoStatus(PromotionStatus.CONFIRMATION_OF_PROMOTION)
                    .build();
            given(promotionHistoryRepository.findByTierPromoStatus(PromotionStatus.CONFIRMATION_OF_PROMOTION))
                    .willReturn(List.of(h1, h2));
            given(tierConfigRepository.findById(20L)).willReturn(Optional.of(buildTierConfig(20L, Grade.A)));
            given(tierConfigRepository.findById(30L)).willReturn(Optional.of(buildTierConfig(30L, Grade.S)));

            // when
            promotionCommandService.applyTierForConfirmed();

            // then
            assertThat(h1.getTierPromoStatus()).isEqualTo(PromotionStatus.TIER_APPLIED);
            assertThat(h2.getTierPromoStatus()).isEqualTo(PromotionStatus.TIER_APPLIED);
            verify(adminClient).updateEmployeeTier(99L, Grade.A);
            verify(adminClient).updateEmployeeTier(88L, Grade.S);
        }

        @Test
        @DisplayName("확정된 이력이 없으면 Admin 호출 없이 종료된다")
        void applyTierForConfirmed_noTargets_doesNothing() {
            // given
            given(promotionHistoryRepository.findByTierPromoStatus(PromotionStatus.CONFIRMATION_OF_PROMOTION))
                    .willReturn(List.of());

            // when
            promotionCommandService.applyTierForConfirmed();

            // then
            verifyNoInteractions(adminClient);
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
            promotionCommandService.suspendPromotion(1000L, 88L);

            // then
            assertThat(history.getTierPromoStatus()).isEqualTo(PromotionStatus.SUSPENSION);
            assertThat(history.getTierReviewedAt()).isNotNull();
            assertThat(history.getReviewerId()).isEqualTo(88L);
        }

        @Test
        @DisplayName("존재하지 않는 승급 이력 보류 시 예외가 발생한다")
        void suspendPromotion_notFound_throwsException() {
            // given
            given(promotionHistoryRepository.findById(9999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> promotionCommandService.suspendPromotion(9999L, 88L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("승급 이력을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("이미 보류된 승급 후보를 다시 보류하면 예외가 발생한다")
        void suspendPromotion_alreadySuspended_throwsException() {
            // given
            PromotionHistory history = buildHistory(PromotionStatus.SUSPENSION);
            given(promotionHistoryRepository.findById(1000L)).willReturn(Optional.of(history));

            // when & then
            assertThatThrownBy(() -> promotionCommandService.suspendPromotion(1000L, 88L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("심사 중인 승급 후보만 처리할 수 있습니다.");
        }
    }
}
