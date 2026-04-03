package com.ohgiraffers.team3backendhr.appeal.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_appeal")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EvaluationAppeal {

    @Id
    @Column(name = "appeal_id")
    private Long appealId;

    @Column(name = "qualitative_evaluation_id", nullable = false)
    private Long qualitativeEvaluationId;

    @Column(name = "appeal_employee_id", nullable = false)
    private Long appealEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "appeal_type", nullable = false)
    private AppealType appealType;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private AppealStatus status = AppealStatus.RECEIVING;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_result")
    private ReviewResult reviewResult;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "anonymized_comparison", nullable = false)
    @Builder.Default
    private Long anonymizedComparison = 0L;

    @Column(name = "filed_at")
    private LocalDateTime filedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "file_group_id", nullable = false)
    private Long fileGroupId;

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

    /* 수정 — RECEIVING 상태에서만 가능 */
    public void update(AppealType appealType, String title, String content) {
        if (this.status != AppealStatus.RECEIVING) {
            throw new IllegalStateException("접수 중인 이의신청만 수정할 수 있습니다.");
        }
        validateTitle(title);
        validateContent(content);
        this.appealType = appealType;
        this.title = title;
        this.content = content;
    }

    /* 취소 가능 여부 검증 — COMPLETED이면 불가 (삭제는 서비스에서 처리) */
    public void cancel() {
        if (this.status == AppealStatus.COMPLETED) {
            throw new IllegalStateException("완료된 이의신청은 취소할 수 없습니다.");
        }
    }

    /* 검토 시작 — RECEIVING → REVIEWING */
    public void startReview(Long reviewerId) {
        if (this.status != AppealStatus.RECEIVING) {
            throw new IllegalStateException("이미 검토 중인 이의신청입니다.");
        }
        this.status = AppealStatus.REVIEWING;
        this.reviewerId = reviewerId;
    }

    /* 승인 — REVIEWING → COMPLETED */
    public void approve(Long reviewedBy, ReviewResult reviewResult) {
        validateReviewing();
        this.status = AppealStatus.COMPLETED;
        this.reviewResult = reviewResult;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
    }

    /* 반려 — REVIEWING → COMPLETED + DISMISS */
    public void reject(Long reviewedBy) {
        validateReviewing();
        this.status = AppealStatus.COMPLETED;
        this.reviewResult = ReviewResult.DISMISS;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
    }

    /* 보류 — REVIEWING 상태 유지 (검토자 기록만) */
    public void hold() {
        validateReviewing();
    }

    private void validateReviewing() {
        if (this.status != AppealStatus.REVIEWING) {
            throw new IllegalStateException("검토 중인 이의신청만 처리할 수 있습니다.");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.length() < 5 || title.length() > 100) {
            throw new IllegalArgumentException("제목은 5자 이상 100자 이하여야 합니다.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.length() < 20 || content.length() > 2000) {
            throw new IllegalArgumentException("내용은 20자 이상 2000자 이하여야 합니다.");
        }
    }
}
