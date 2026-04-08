package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notificationrecipient.NotificationRecipient;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NotificationRecipientRepository;
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

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceTest {

    @Mock
    private NotificationRecipientRepository notificationRecipientRepository;

    @InjectMocks
    private NotificationCommandService notificationCommandService;

    @Nested
    @DisplayName("hide 메서드")
    class Hide {

        @Test
        @DisplayName("알림이 숨김 처리된다")
        void hide_setsIsHideTrue() {
            // given
            NotificationRecipient recipient = NotificationRecipient.builder()
                    .notificationRecipientId(1000L)
                    .notificationId(500L)
                    .employeeId(99L)
                    .build();

            given(notificationRecipientRepository.findByNotificationIdAndEmployeeId(500L, 99L))
                    .willReturn(Optional.of(recipient));

            // when
            notificationCommandService.hide(500L, 99L);

            // then
            assertThat(recipient.getNotificationIsHide()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 알림 숨김 시 예외가 발생한다")
        void hide_notFound_throwsException() {
            // given
            given(notificationRecipientRepository.findByNotificationIdAndEmployeeId(9999L, 99L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationCommandService.hide(9999L, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("알림 수신 정보를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("acknowledge 메서드")
    class Acknowledge {

        @Test
        @DisplayName("알림이 읽음 처리된다")
        void acknowledge_setsIsReadTrue() {
            // given
            NotificationRecipient recipient = NotificationRecipient.builder()
                    .notificationRecipientId(1000L)
                    .notificationId(500L)
                    .employeeId(99L)
                    .build();

            given(notificationRecipientRepository.findByNotificationIdAndEmployeeId(500L, 99L))
                    .willReturn(Optional.of(recipient));

            // when
            notificationCommandService.acknowledge(500L, 99L);

            // then
            assertThat(recipient.getNotificationIsRead()).isTrue();
            assertThat(recipient.getNotificationReadAt()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 알림 읽음 처리 시 예외가 발생한다")
        void acknowledge_notFound_throwsException() {
            // given
            given(notificationRecipientRepository.findByNotificationIdAndEmployeeId(9999L, 99L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationCommandService.acknowledge(9999L, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("알림 수신 정보를 찾을 수 없습니다.");
        }
    }
}
