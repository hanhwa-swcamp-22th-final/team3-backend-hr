package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNotificationRepository extends JpaRepository<Notification, Long> {
}
