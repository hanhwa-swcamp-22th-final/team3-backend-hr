package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NotificationRecipient;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NotificationRecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {

    private final NotificationRecipientRepository notificationRecipientRepository;

    public void hide(Long notificationId, Long employeeId) {
        NotificationRecipient recipient = notificationRecipientRepository
                .findByNotificationIdAndEmployeeId(notificationId, employeeId)
                .orElseThrow(() -> new IllegalArgumentException("알림 수신 정보를 찾을 수 없습니다."));
        recipient.hide();
    }

    public void acknowledge(Long notificationId, Long employeeId) {
        NotificationRecipient recipient = notificationRecipientRepository
                .findByNotificationIdAndEmployeeId(notificationId, employeeId)
                .orElseThrow(() -> new IllegalArgumentException("알림 수신 정보를 찾을 수 없습니다."));
        recipient.acknowledge();
    }
}
