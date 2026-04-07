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
@Table(name = "notice")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_status", nullable = false)
    private NoticeStatus noticeStatus;

    @Column(name = "is_important", nullable = false)
    @Builder.Default
    private Integer isImportant = 0;

    @Column(name = "notice_title", nullable = false)
    private String noticeTitle;

    @Column(name = "notice_content", nullable = false, columnDefinition = "TEXT")
    private String noticeContent;

    @Column(name = "notice_views", nullable = false)
    @Builder.Default
    private Long noticeViews = 0L;

    @Column(name = "publish_start_at")
    private LocalDateTime publishStartAt;

    @Column(name = "important_end_at")
    private LocalDateTime importantEndAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    public void update(String title, String content, NoticeStatus status,
                       boolean important, LocalDateTime publishStartAt, LocalDateTime importantEndAt) {
        this.noticeTitle = title;
        this.noticeContent = content;
        this.noticeStatus = status;
        this.isImportant = important ? 1 : 0;
        this.publishStartAt = publishStartAt;
        this.importantEndAt = importantEndAt;
    }

    /* 예약 → 게시중 전환 */
    public void publish() {
        this.noticeStatus = NoticeStatus.POSTING;
    }

    /* 중요 공지 만료 */
    public void expireImportant() {
        this.isImportant = 0;
    }

    public void incrementViews() {
        this.noticeViews++;
    }
}
