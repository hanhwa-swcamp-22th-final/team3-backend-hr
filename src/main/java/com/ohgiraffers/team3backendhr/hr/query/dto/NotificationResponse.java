package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long notificationRecipientId;
    private Long notificationId;
    private String notificationType;
    private String notificationTitle;
    private String notificationContent;
    private LocalDateTime notificationSentAt;
    private Boolean notificationIsRead;
    private LocalDateTime notificationReadAt;
}
