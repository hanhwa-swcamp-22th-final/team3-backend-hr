package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NotificationRecipientTest {

    private NotificationRecipient unread() {
        return NotificationRecipient.builder()
                .notificationRecipientId(1L)
                .notificationId(100L)
                .employeeId(10L)
                .build();
    }

    @Test
    @DisplayName("hide 호출 시 notificationIsHide 가 true 가 된다")
    void hide_setsIsHideTrue() {
        NotificationRecipient recipient = unread();
        assertThat(recipient.getNotificationIsHide()).isFalse();

        recipient.hide();

        assertThat(recipient.getNotificationIsHide()).isTrue();
    }

    @Test
    @DisplayName("acknowledge 호출 시 notificationIsRead 가 true 가 되고 readAt 이 기록된다")
    void acknowledge_setsIsReadAndRecordsTime() {
        NotificationRecipient recipient = unread();
        assertThat(recipient.getNotificationIsRead()).isFalse();

        recipient.acknowledge();

        assertThat(recipient.getNotificationIsRead()).isTrue();
        assertThat(recipient.getNotificationReadAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 읽은 알림에 acknowledge 를 재호출해도 readAt 이 덮어씌워지지 않는다")
    void acknowledge_alreadyRead_doesNotOverwriteReadAt() {
        NotificationRecipient recipient = unread();
        recipient.acknowledge();
        var firstReadAt = recipient.getNotificationReadAt();

        // 짧은 지연 없이 재호출 — LocalDateTime.now() 는 동일 ms 일 수 있으므로 값 자체 변경 여부만 확인
        recipient.acknowledge();

        assertThat(recipient.getNotificationReadAt()).isEqualTo(firstReadAt);
    }

    @Test
    @DisplayName("hide 와 acknowledge 는 독립적으로 동작한다")
    void hide_and_acknowledge_areIndependent() {
        NotificationRecipient recipient = unread();

        recipient.hide();
        recipient.acknowledge();

        assertThat(recipient.getNotificationIsHide()).isTrue();
        assertThat(recipient.getNotificationIsRead()).isTrue();
    }
}
