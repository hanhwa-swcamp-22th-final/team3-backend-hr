package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_recipient")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationRecipient {

    @Id
    @Column(name = "notification_recipient_id")
    private Long notificationRecipientId;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "notification_is_read", nullable = false)
    @Builder.Default
    private Boolean notificationIsRead = false;

    @Column(name = "notification_is_hide", nullable = false)
    @Builder.Default
    private Boolean notificationIsHide = false;

    @Column(name = "notification_read_at")
    private LocalDateTime notificationReadAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    /* 확인 버튼 클릭 시 목록에서 제거 — notification_is_hide = TRUE */
    public void hide() {
        this.notificationIsHide = true;
    }

    /* 알림 읽음 처리 */
    public void acknowledge() {
        if (!this.notificationIsRead) {
            this.notificationIsRead = true;
            this.notificationReadAt = LocalDateTime.now();
        }
    }
}
