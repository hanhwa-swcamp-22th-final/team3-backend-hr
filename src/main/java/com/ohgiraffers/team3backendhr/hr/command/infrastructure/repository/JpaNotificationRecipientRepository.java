package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notificationrecipient.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaNotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {
    List<NotificationRecipient> findByEmployeeId(Long employeeId);
    List<NotificationRecipient> findByEmployeeIdAndNotificationIsHideFalse(Long employeeId);
    long countByEmployeeIdAndNotificationIsReadFalseAndNotificationIsHideFalse(Long employeeId);
    java.util.Optional<NotificationRecipient> findByNotificationIdAndEmployeeId(Long notificationId, Long employeeId);
}
