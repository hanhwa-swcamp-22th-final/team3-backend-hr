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
@Table(name = "score_modification_log")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ScoreModificationLog {

    @Id
    @Column(name = "score_modification_log_id")
    private Long scoreModificationLogId;

    @Column(name = "score_evaluatee_id", nullable = false)
    private Long scoreEvaluateeId;

    @Column(name = "score_modifier_id", nullable = false)
    private Long scoreModifierId;

    @Column(name = "score_original_score")
    private Double scoreOriginalScore;

    @Column(name = "score_modified_score")
    private Double scoreModifiedScore;

    @Column(name = "score_reason", length = 500)
    private String scoreReason;

    @Column(name = "score_is_deletable")
    @Builder.Default
    private Boolean scoreIsDeletable = false;   // 항상 false

    @Column(name = "score_modified_at")
    private LocalDateTime scoreModifiedAt;

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
}
