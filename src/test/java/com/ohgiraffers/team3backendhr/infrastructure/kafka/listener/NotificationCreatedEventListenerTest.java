package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ohgiraffers.team3backendhr.hr.command.application.service.NotificationCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notification.NotificationType;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.NotificationCreatedEvent;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationCreatedEventListenerTest {

    @Mock
    private NotificationCommandService notificationCommandService;

    @InjectMocks
    private NotificationCreatedEventListener listener;

    @Test
    @DisplayName("알림 생성 이벤트를 수신하면 알림을 생성한다")
    void listen_createsNotification() {
        // given
        NotificationCreatedEvent event = new NotificationCreatedEvent(
                "ARRANGEMENT",
                "작업 배정 알림",
                "새 작업 배정이 있습니다.",
                List.of(10L, 20L)
        );

        // when
        listener.listen(event);

        // then
        then(notificationCommandService).should().create(
                NotificationType.ARRANGEMENT,
                "작업 배정 알림",
                "새 작업 배정이 있습니다.",
                List.of(10L, 20L)
        );
    }

    @Test
    @DisplayName("알 수 없는 알림 타입은 무시한다")
    void listen_ignoresUnknownType() {
        // given
        NotificationCreatedEvent event = new NotificationCreatedEvent(
                "UNKNOWN",
                "제목",
                "내용",
                List.of(10L)
        );

        // when
        listener.listen(event);

        // then
        then(notificationCommandService).should(never()).create(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    @DisplayName("수신자가 없으면 알림 생성을 건너뛴다")
    void listen_ignoresEmptyRecipients() {
        // given
        NotificationCreatedEvent event = new NotificationCreatedEvent(
                "ARRANGEMENT",
                "제목",
                "내용",
                List.of()
        );

        // when
        listener.listen(event);

        // then
        then(notificationCommandService).should(never()).create(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
