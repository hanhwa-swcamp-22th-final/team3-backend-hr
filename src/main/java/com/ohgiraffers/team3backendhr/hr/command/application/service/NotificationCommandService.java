package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notificationrecipient.NotificationRecipient;
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
