package com.ohgiraffers.team3backendhr.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendhr.hr.command.application.service.NotificationCommandService;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notification.NotificationType;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.NotificationCreatedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.support.NotificationKafkaTopics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCreatedEventListener {

    private final NotificationCommandService notificationCommandService;

    @KafkaListener(
            topics = NotificationKafkaTopics.NOTIFICATION_CREATED,
            containerFactory = "notificationCreatedKafkaListenerContainerFactory"
    )
    public void listen(NotificationCreatedEvent event) {
        if (event == null) {
            return;
        }

        NotificationType notificationType;
        try {
            notificationType = NotificationType.valueOf(event.getNotificationType());
        } catch (IllegalArgumentException | NullPointerException exception) {
            log.warn("[Notification] 알 수 없는 notificationType: {}", event.getNotificationType());
            return;
        }

        List<Long> recipientIds = event.getRecipientIds();
        if (recipientIds == null || recipientIds.isEmpty()) {
            log.debug("[Notification] 수신자가 없어 알림 생성을 건너뜁니다. type={}", notificationType);
            return;
        }

        notificationCommandService.create(
                notificationType,
                event.getTitle(),
                event.getContent(),
                recipientIds
        );
        log.info(
                "[Notification] 이벤트 수신 — type={}, recipientCount={}",
                notificationType,
                recipientIds.size()
        );
    }
}
