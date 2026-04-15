package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
@Entity
@Table(name = "qualitative_evaluation")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class QualitativeEvaluation {

    @Id
    @Column(name = "qualitative_evaluation_id")
    private Long qualitativeEvaluationId;

    @Column(name = "evaluatee_id", nullable = false)
    private Long evaluateeId;

    @Column(name = "evaluator_id")
    private Long evaluatorId;

    @Column(name = "evaluation_period_id", nullable = false)
    private Long evaluationPeriodId;

    @Column(name = "evaluation_level")
    private Long evaluationLevel;

    @Column(name = "eval_items", columnDefinition = "JSON")
    private String evalItems;

    @Column(name = "eval_comment", length = 2000)
    private String evalComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade")
    private Grade grade;

    @Column(name = "score")
    private Double score;

    @Column(name = "s_qual")
    private Double sQual;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_method")
    private InputMethod inputMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private QualEvalStatus status = QualEvalStatus.NO_INPUT;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

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

    public void saveDraft(Long evaluatorId, String evalItems, String evalComment, InputMethod inputMethod) {
        if (this.status == QualEvalStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.EVALUATION_ALREADY_SUBMITTED);
        }
        if (this.status == QualEvalStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.EVALUATION_ALREADY_CONFIRMED);
        }
        this.evaluatorId = evaluatorId;
        this.evalItems = evalItems;
        this.evalComment = evalComment;
        this.inputMethod = inputMethod;
        this.status = QualEvalStatus.DRAFT;
    }

    public void submit(Long evaluatorId, String evalItems, String evalComment, InputMethod inputMethod) {
        if (this.status == QualEvalStatus.SUBMITTED || this.status == QualEvalStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.EVALUATION_ALREADY_SUBMITTED);
        }
        if (evalComment == null || evalComment.length() < 20) {
            throw new BusinessException(ErrorCode.INVALID_COMMENT_LENGTH);
        }
        this.evaluatorId = evaluatorId;
        this.evalItems = evalItems;
        this.evalComment = evalComment;
        this.inputMethod = inputMethod;
        this.status = QualEvalStatus.SUBMITTED;
    }

    public void applyAnalysisResult(Double rawScore) {
        if (this.status != QualEvalStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_SUBMITTED);
        }
        this.score = rawScore;
    }

    public void updateScore(Double score) {
        this.score = score;
    }

    public void applyNormalizationResult(Double sQual, Grade grade) {
        this.sQual = sQual;
        this.grade = grade;
    }

    public void confirmFinal(Long evaluatorId, String evalComment, InputMethod inputMethod) {
        if (this.status == QualEvalStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.EVALUATION_ALREADY_CONFIRMED);
        }
        if (evalComment == null || evalComment.length() < 20) {
            throw new BusinessException(ErrorCode.INVALID_COMMENT_LENGTH);
        }
        this.evaluatorId = evaluatorId;
        this.evalComment = evalComment;
        this.inputMethod = inputMethod;
        this.status = QualEvalStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void reopenForAppealReview() {
        if (this.status != QualEvalStatus.SUBMITTED && this.status != QualEvalStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이의신청 검토로 재오픈할 수 없는 평가 상태입니다.");
        }
        this.status = QualEvalStatus.DRAFT;
        this.confirmedAt = null;
    }
}
