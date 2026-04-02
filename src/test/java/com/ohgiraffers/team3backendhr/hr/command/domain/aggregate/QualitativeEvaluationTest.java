package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class QualitativeEvaluationTest {

    private static final Long EVALUATOR_ID = 50L;

    /**
     * 테스트용 QualitativeEvaluation 객체 생성 헬퍼.
     * 평가기간 생성 시 level별로 레코드가 하나씩 만들어지므로,
     * evaluationLevel은 생성 시 고정값으로 주입하고 메서드 내에서 변경하지 않는다.
     * evaluatorId, grade, inputMethod는 생성 시 null — 실제 평가 시 세팅된다.
     */
    private QualitativeEvaluation buildEval(QualEvalStatus status, Long evaluationLevel) {
        return QualitativeEvaluation.builder()
                .qualitativeEvaluationId(1L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .status(status)
                .evaluationLevel(evaluationLevel)
                .build();
    }

    /* ── saveDraft (임시저장, level 1·2 공용) ──────────────────────────────── */

    @Test
    @DisplayName("NO_INPUT 상태에서 임시저장 시 DRAFT로 전이되고 evaluatorId가 세팅된다")
    void saveDraft_success_fromNoInput() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT, 1L);

        // when
        eval.saveDraft(EVALUATOR_ID, "{\"TECHNICAL_COMPETENCE\": 90}", "이번 분기 설비 대응 역량이 크게 향상되었습니다.", InputMethod.TEXT);

        // then
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.DRAFT);
        assertThat(eval.getEvaluatorId()).isEqualTo(EVALUATOR_ID);
        assertThat(eval.getEvalItems()).isEqualTo("{\"TECHNICAL_COMPETENCE\": 90}");
        assertThat(eval.getEvalComment()).isEqualTo("이번 분기 설비 대응 역량이 크게 향상되었습니다.");
        assertThat(eval.getEvaluationLevel()).isEqualTo(1L); // evaluationLevel은 변하지 않는다
    }

    @Test
    @DisplayName("DRAFT 상태에서 임시저장 시 DRAFT를 유지한다")
    void saveDraft_success_fromDraft() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.DRAFT, 1L);

        // when
        eval.saveDraft(EVALUATOR_ID, "{\"TECHNICAL_COMPETENCE\": 95}", "이번 분기 설비 대응 역량이 크게 향상되었습니다.", InputMethod.TEXT);

        // then
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.DRAFT);
    }

    @Test
    @DisplayName("SUBMITTED 상태에서 임시저장 시 예외가 발생한다")
    void saveDraft_fail_alreadySubmitted() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.SUBMITTED, 1L);

        // when & then
        assertThatThrownBy(() -> eval.saveDraft(EVALUATOR_ID, "{}", "이번 분기 설비 대응 역량이 크게 향상되었습니다.", InputMethod.TEXT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 제출된 평가는 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("CONFIRMED 상태에서 임시저장 시 예외가 발생한다")
    void saveDraft_fail_alreadyConfirmed() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.CONFIRMED, 1L);

        // when & then
        assertThatThrownBy(() -> eval.saveDraft(EVALUATOR_ID, "{}", "이번 분기 설비 대응 역량이 크게 향상되었습니다.", InputMethod.TEXT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 확정된 평가는 수정할 수 없습니다.");
    }

    /* ── submit (평가 제출, level 1·2 공용) ────────────────────────────────── */

    @Test
    @DisplayName("NO_INPUT 상태에서 제출 시 SUBMITTED로 전이되고 evaluatorId·score·grade가 세팅된다")
    void submit_success_fromNoInput() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT, 1L);

        // when — grade는 서비스에서 score 기반으로 산출 후 전달
        eval.submit(EVALUATOR_ID, "{\"TECHNICAL_COMPETENCE\": 90}", "이번 분기 설비 대응 역량이 크게 향상되었습니다.", InputMethod.TEXT, 88.0, Grade.A);

        // then
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.SUBMITTED);
        assertThat(eval.getEvaluatorId()).isEqualTo(EVALUATOR_ID);
        assertThat(eval.getScore()).isEqualTo(88.0);
        assertThat(eval.getGrade()).isEqualTo(Grade.A);
        assertThat(eval.getEvaluationLevel()).isEqualTo(1L); // evaluationLevel은 변하지 않는다
    }

    @Test
    @DisplayName("DRAFT 상태에서 제출 시 SUBMITTED로 전이된다")
    void submit_success_fromDraft() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.DRAFT, 2L);

        // when
        eval.submit(EVALUATOR_ID, "{\"TECHNICAL_COMPETENCE\": 90}", "이번 분기 설비 대응 역량이 크게 향상되었습니다.", InputMethod.TEXT, 75.0, Grade.B);

        // then
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.SUBMITTED);
        assertThat(eval.getEvaluationLevel()).isEqualTo(2L); // level 2 레코드도 동일하게 동작
    }

    @Test
    @DisplayName("평가 코멘트가 20자 미만이면 제출 시 예외가 발생한다")
    void submit_fail_commentTooShort() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT, 1L);

        // when & then
        assertThatThrownBy(() -> eval.submit(EVALUATOR_ID, "{}", "짧은 코멘트", InputMethod.TEXT, 88.0, Grade.A))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평가 코멘트는 최소 20자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("평가 코멘트가 null이면 제출 시 예외가 발생한다")
    void submit_fail_commentNull() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT, 1L);

        // when & then
        assertThatThrownBy(() -> eval.submit(EVALUATOR_ID, "{}", null, InputMethod.TEXT, 88.0, Grade.A))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평가 코멘트는 최소 20자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("SUBMITTED 상태에서 재제출 시 예외가 발생한다")
    void submit_fail_alreadySubmitted() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.SUBMITTED, 1L);

        // when & then
        assertThatThrownBy(() -> eval.submit(EVALUATOR_ID, "{}", "이번 분기 설비 대응 역량이 크게 향상되었습니다.", InputMethod.TEXT, 88.0, Grade.A))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 제출된 평가입니다.");
    }

    @Test
    @DisplayName("CONFIRMED 상태에서 제출 시 예외가 발생한다")
    void submit_fail_alreadyConfirmed() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.CONFIRMED, 1L);

        // when & then
        assertThatThrownBy(() -> eval.submit(EVALUATOR_ID, "{}", "이번 분기 설비 대응 역량이 크게 향상되었습니다.", InputMethod.TEXT, 88.0, Grade.A))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 제출된 평가입니다.");
    }

    /* ── confirmFinal (HRM 최종 확정, level 3 전용) ────────────────────────── */

    @Test
    @DisplayName("NO_INPUT 상태에서 최종 확정 시 CONFIRMED로 전이되고 evaluatorId·코멘트가 세팅된다")
    void confirmFinal_success() {
        // given — level=3 레코드, 2차 완료 여부는 서비스에서 체크하므로 엔티티는 단순히 상태만 전이
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT, 3L);

        // when
        eval.confirmFinal(EVALUATOR_ID, "전반적으로 우수한 성과를 보여준 직원입니다. 다음 분기도 기대합니다.", InputMethod.TEXT);

        // then
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.CONFIRMED);
        assertThat(eval.getEvaluatorId()).isEqualTo(EVALUATOR_ID);
        assertThat(eval.getEvalComment()).isEqualTo("전반적으로 우수한 성과를 보여준 직원입니다. 다음 분기도 기대합니다.");
        assertThat(eval.getEvaluationLevel()).isEqualTo(3L);
    }

    @Test
    @DisplayName("HRM 최종 확정 시 코멘트가 20자 미만이면 예외가 발생한다")
    void confirmFinal_fail_commentTooShort() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT, 3L);

        // when & then
        assertThatThrownBy(() -> eval.confirmFinal(EVALUATOR_ID, "짧은 코멘트", InputMethod.TEXT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평가 코멘트는 최소 20자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("HRM 최종 확정 시 코멘트가 null이면 예외가 발생한다")
    void confirmFinal_fail_commentNull() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT, 3L);

        // when & then
        assertThatThrownBy(() -> eval.confirmFinal(EVALUATOR_ID, null, InputMethod.TEXT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평가 코멘트는 최소 20자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("이미 확정된 상태에서 재확정 시 예외가 발생한다")
    void confirmFinal_fail_alreadyConfirmed() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.CONFIRMED, 3L);

        // when & then
        assertThatThrownBy(() -> eval.confirmFinal(EVALUATOR_ID, "전반적으로 우수한 성과를 보여준 직원입니다. 다음 분기도 기대합니다.", InputMethod.TEXT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 확정된 평가입니다.");
    }
}
