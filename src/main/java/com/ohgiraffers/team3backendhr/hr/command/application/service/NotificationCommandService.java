package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notification.Notification;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notification.NotificationType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notificationrecipient.NotificationRecipient;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NotificationRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NotificationRecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {

    private final NotificationRecipientRepository notificationRecipientRepository;
    private final NotificationRepository notificationRepository;
    private final IdGenerator idGenerator;

    public void create(NotificationType type, String title, String content, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) return;

        Notification notification = Notification.builder()
                .notificationId(idGenerator.generate())
                .notificationType(type)
                .notificationTitle(title)
                .notificationContent(content)
                .notificationSentAt(LocalDateTime.now())
                .build();
        Notification saved = notificationRepository.save(notification);

        List<NotificationRecipient> recipients = recipientIds.stream()
                .filter(id -> id != null)
                .map(empId -> NotificationRecipient.builder()
                        .notificationRecipientId(idGenerator.generate())
                        .notificationId(saved.getNotificationId())
                        .employeeId(empId)
                        .build())
                .toList();
        notificationRecipientRepository.saveAll(recipients);
    }

    public void hide(Long notificationId, Long employeeId) {
        NotificationRecipient recipient = notificationRecipientRepository
                .findByNotificationIdAndEmployeeId(notificationId, employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        recipient.hide();
    }

    public void acknowledge(Long notificationId, Long employeeId) {
        NotificationRecipient recipient = notificationRecipientRepository
                .findByNotificationIdAndEmployeeId(notificationId, employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        recipient.acknowledge();
    }
}
