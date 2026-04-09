package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcomment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluation_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EvaluationComment {

    @Id
    @Column(name = "evaluation_comment_id")
    private Long evaluationCommentId;

    @Column(name = "qualitative_evaluation_id", nullable = false)
    private Long qualitativeEvaluationId;

    @Column(name = "algorithm_version_id", nullable = false)
    private Long algorithmVersionId;

    @Column(name = "nlp_sentiment")
    private BigDecimal nlpSentiment;

    @Column(name = "matched_keyword_count", nullable = false)
    private Integer matchedKeywordCount;

    @Column(name = "matched_keywords", columnDefinition = "JSON")
    private String matchedKeywords;

    @Column(name = "context_weight")
    private BigDecimal contextWeight;

    @Column(name = "negation_detected", nullable = false)
    private Boolean negationDetected;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}