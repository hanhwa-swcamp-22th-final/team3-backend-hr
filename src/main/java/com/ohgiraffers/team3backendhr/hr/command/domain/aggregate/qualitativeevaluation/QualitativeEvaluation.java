package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation;


import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "qualitative_evaluation")
@EntityListeners(AuditingEntityListener.class) // createdAt, createdBy 등 Auditing 자동 처리
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자 필요, PROTECTED로 외부 직접 생성 방지
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // @Builder가 내부적으로 사용하는 전체 생성자 (외부 직접 생성 방지)
@Builder
public class QualitativeEvaluation {

    @Id
    @Column(name = "qualitative_evaluation_id")
    private Long qualitativeEvaluationId;

    @Column(name = "evaluatee_id", nullable = false)
    private Long evaluateeId;           // 피평가자 ID

    @Column(name = "evaluator_id")
    private Long evaluatorId;           // 평가자 ID — 생성 시 null, 실제 평가 시 로그인한 사람 ID로 세팅

    @Column(name = "evaluation_period_id", nullable = false)
    private Long evaluationPeriodId;

    @Column(name = "evaluation_level")
    private Long evaluationLevel;       // 평가 차수: 1=TL(1차), 2=DL(2차), 3=HRM(최종)

    @Column(name = "eval_items", columnDefinition = "JSON")
    private String evalItems;           // 카테고리별 점수 JSON (예: {"TECHNICAL_COMPETENCE": 90})

    @Column(name = "eval_comment", length = 2000)
    private String evalComment;         // 평가 코멘트 (최소 20자, 최대 2000자)

    @Enumerated(EnumType.STRING)
    @Column(name = "grade")
    private Grade grade;                // S / A / B / C — score 기반으로 서비스에서 자동 산출, 생성 시 null

    @Column(name = "score")
    private Double score;               // batch NLP 분석 결과로 세팅되는 점수 (0~100)

    @Enumerated(EnumType.STRING)
    @Column(name = "input_method")
    private InputMethod inputMethod;    // TEXT / VOICE_STT — 생성 시 null, 실제 입력 시 세팅

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default                    // @Builder 사용 시 기본값 적용을 위해 필요
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

    /* 임시저장 — NO_INPUT or DRAFT → DRAFT (level 1·2 공용, evaluationLevel은 생성 시 고정) */
    public void saveDraft(Long evaluatorId, String evalItems, String evalComment, InputMethod inputMethod) {
        if (this.status == QualEvalStatus.SUBMITTED) {
            throw new IllegalStateException("이미 제출된 평가는 수정할 수 없습니다.");
        }
        if (this.status == QualEvalStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 평가는 수정할 수 없습니다.");
        }
        this.evaluatorId = evaluatorId;  // 로그인한 평가자 ID 세팅
        this.evalItems = evalItems;
        this.evalComment = evalComment;
        this.inputMethod = inputMethod;
        this.status = QualEvalStatus.DRAFT;
    }

    /* 평가 제출 — NO_INPUT or DRAFT → SUBMITTED (level 1·2 공용) */
    /* score·grade는 batch NLP 분석 후 applyAnalysisResult()로 세팅 */
    public void submit(Long evaluatorId, String evalItems, String evalComment, InputMethod inputMethod) {
        if (this.status == QualEvalStatus.SUBMITTED || this.status == QualEvalStatus.CONFIRMED) {
            throw new IllegalStateException("이미 제출된 평가입니다.");
        }
        if (evalComment == null || evalComment.length() < 20) {
            throw new IllegalArgumentException("평가 코멘트는 최소 20자 이상이어야 합니다.");
        }
        this.evaluatorId = evaluatorId;
        this.evalItems = evalItems;
        this.evalComment = evalComment;
        this.inputMethod = inputMethod;
        this.status = QualEvalStatus.SUBMITTED;
    }

    /* batch NLP 분석 결과 반영 — SUBMITTED 상태에서만 허용 */
    public void applyAnalysisResult(Double score, Grade grade) {
        if (this.status != QualEvalStatus.SUBMITTED) {
            throw new IllegalStateException("제출된 평가에만 분석 결과를 반영할 수 있습니다.");
        }
        this.score = score;
        this.grade = grade;
    }

    /* HRM 최종 확정 — NO_INPUT → CONFIRMED (level 3 전용) */
    /* 2차 완료 여부 체크는 서비스에서 수행 */
    public void confirmFinal(Long evaluatorId, String evalComment, InputMethod inputMethod) {
        if (this.status == QualEvalStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 평가입니다.");
        }
        if (evalComment == null || evalComment.length() < 20) {
            throw new IllegalArgumentException("평가 코멘트는 최소 20자 이상이어야 합니다.");
        }
        this.evaluatorId = evaluatorId;
        this.evalComment = evalComment;
        this.inputMethod = inputMethod;
        this.status = QualEvalStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }
}
