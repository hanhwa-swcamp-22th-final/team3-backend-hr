package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;

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

    @Column(name = "appeal_employee_id", nullable = false)
    private Long appealEmployeeId;

    @Column(name = "evaluation_period_id", nullable = false)
    private Long evaluationPeriodId;

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
    private AppealStatus status = AppealStatus.SUBMITTED;

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

    /* 수정 — SUBMITTED 상태에서만 가능 */
    public void update(AppealType appealType, String title, String content) {
        if (this.status != AppealStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.APPEAL_NOT_RECEIVABLE);
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
            throw new BusinessException(ErrorCode.APPEAL_ALREADY_COMPLETED);
        }
    }

    /* HRM 접수 — SUBMITTED → RECEIVING */
    public void receive(Long reviewerId) {
        if (this.status != AppealStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.APPEAL_ALREADY_REVIEWING);
        }
        this.status = AppealStatus.RECEIVING;
        this.reviewerId = reviewerId;
    }

    /* TL 승인 후 DL 검토 단계 진입 — RECEIVING → REVIEWING */
    public void startReview(Long reviewerId) {
        if (this.status != AppealStatus.RECEIVING) {
            throw new BusinessException(ErrorCode.APPEAL_NOT_REVIEWING);
        }
        this.status = AppealStatus.REVIEWING;
        this.reviewerId = reviewerId;
    }

    /* 승인 — 진행 중 상태 → COMPLETED */
    public void approve(Long reviewedBy, ReviewResult reviewResult) {
        validateProcessable();
        this.status = AppealStatus.COMPLETED;
        this.reviewResult = reviewResult;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
    }

    /* 반려 — 진행 중 상태 → COMPLETED + DISMISS */
    public void reject(Long reviewedBy) {
        validateProcessable();
        this.status = AppealStatus.COMPLETED;
        this.reviewResult = ReviewResult.DISMISS;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
    }

    /* 보류 — REVIEWING 상태 유지 (검토자 기록만) */
    public void hold() {
        if (this.status != AppealStatus.RECEIVING && this.status != AppealStatus.REVIEWING) {
            throw new BusinessException(ErrorCode.APPEAL_NOT_REVIEWING);
        }
    }

    private void validateProcessable() {
        if (this.status != AppealStatus.SUBMITTED
            && this.status != AppealStatus.RECEIVING
            && this.status != AppealStatus.REVIEWING) {
            throw new BusinessException(ErrorCode.APPEAL_NOT_REVIEWING);
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.length() < 5 || title.length() > 100) {
            throw new BusinessException(ErrorCode.INVALID_TITLE_LENGTH);
        }
    }

    private void validateContent(String content) {
        if (content == null || content.length() < 20 || content.length() > 2000) {
            throw new BusinessException(ErrorCode.INVALID_CONTENT_LENGTH);
        }
    }
}
