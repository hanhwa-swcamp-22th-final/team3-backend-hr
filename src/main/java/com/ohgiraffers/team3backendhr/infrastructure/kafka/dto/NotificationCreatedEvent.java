package com.ohgiraffers.team3backendhr.infrastructure.kafka.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreatedEvent {
    private String notificationType;
    private String title;
    private String content;
    private List<Long> recipientIds;
}
