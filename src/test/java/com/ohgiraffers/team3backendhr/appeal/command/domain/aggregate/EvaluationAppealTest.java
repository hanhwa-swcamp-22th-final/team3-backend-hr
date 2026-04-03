package com.ohgiraffers.team3backendhr.appeal.command.domain.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EvaluationAppealTest {

    private EvaluationAppeal buildAppeal(AppealStatus status) {
        return EvaluationAppeal.builder()
                .appealId(1L)
                .qualitativeEvaluationId(10L)
                .appealEmployeeId(100L)
                .appealType(AppealType.SCORE_ERRORS)
                .title("점수 오류 이의신청")
                .content("평가 점수에 명백한 오류가 있습니다. 재검토 요청드립니다.")
                .status(status)
                .fileGroupId(99L)
                .build();
    }

    /* ───── update ───── */

    @Test
    @DisplayName("RECEIVING 상태에서 내용을 수정할 수 있다")
    void update_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.RECEIVING);

        appeal.update(AppealType.MISSING_ITEMS, "수정된 제목입니다", "수정된 내용입니다. 20자 이상 작성합니다.");

        assertThat(appeal.getAppealType()).isEqualTo(AppealType.MISSING_ITEMS);
        assertThat(appeal.getTitle()).isEqualTo("수정된 제목입니다");
    }

    @Test
    @DisplayName("REVIEWING 상태에서 수정 시 예외가 발생한다")
    void update_fail_notReceiving() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.REVIEWING);

        assertThatThrownBy(() -> appeal.update(AppealType.OTHERS, "제목", "20자 이상의 수정 내용입니다."))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("접수 중인 이의신청만 수정할 수 있습니다.");
    }

    @Test
    @DisplayName("제목이 5자 미만이면 예외가 발생한다")
    void update_fail_titleTooShort() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.RECEIVING);

        assertThatThrownBy(() -> appeal.update(AppealType.OTHERS, "짧음", "20자 이상의 수정 내용입니다."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 5자 이상 100자 이하여야 합니다.");
    }

    @Test
    @DisplayName("내용이 20자 미만이면 예외가 발생한다")
    void update_fail_contentTooShort() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.RECEIVING);

        assertThatThrownBy(() -> appeal.update(AppealType.OTHERS, "정상적인 제목", "짧은 내용"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("내용은 20자 이상 2000자 이하여야 합니다.");
    }

    /* ───── cancel ───── */

    @Test
    @DisplayName("RECEIVING 상태에서 취소할 수 있다")
    void cancel_success_receiving() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.RECEIVING);

        appeal.cancel();

        // cancel은 soft-delete 또는 별도 상태 없이 repository에서 delete 처리
        // 엔티티 레벨에서 COMPLETED로 가지 않음 — 서비스에서 delete 호출
        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.RECEIVING); // 상태 변경 없음, 서비스에서 delete
    }

    @Test
    @DisplayName("COMPLETED 상태에서 취소 시 예외가 발생한다")
    void cancel_fail_completed() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.COMPLETED);

        assertThatThrownBy(() -> appeal.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("완료된 이의신청은 취소할 수 없습니다.");
    }

    /* ───── startReview ───── */

    @Test
    @DisplayName("RECEIVING 상태에서 검토 시작 시 REVIEWING으로 변경된다")
    void startReview_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.RECEIVING);

        appeal.startReview(200L);

        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.REVIEWING);
        assertThat(appeal.getReviewerId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("REVIEWING 상태에서 검토 시작 시 예외가 발생한다")
    void startReview_fail_alreadyReviewing() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.REVIEWING);

        assertThatThrownBy(() -> appeal.startReview(200L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 검토 중인 이의신청입니다.");
    }

    /* ───── approve ───── */

    @Test
    @DisplayName("REVIEWING 상태에서 승인하면 COMPLETED·review_result가 설정된다")
    void approve_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.REVIEWING);

        appeal.approve(200L, ReviewResult.ACKNOWLEDGE);

        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.COMPLETED);
        assertThat(appeal.getReviewResult()).isEqualTo(ReviewResult.ACKNOWLEDGE);
        assertThat(appeal.getReviewedBy()).isEqualTo(200L);
        assertThat(appeal.getReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("RECEIVING 상태에서 승인 시 예외가 발생한다")
    void approve_fail_notReviewing() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.RECEIVING);

        assertThatThrownBy(() -> appeal.approve(200L, ReviewResult.ACKNOWLEDGE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("검토 중인 이의신청만 처리할 수 있습니다.");
    }

    /* ───── reject ───── */

    @Test
    @DisplayName("REVIEWING 상태에서 반려하면 COMPLETED·DISMISS가 설정된다")
    void reject_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.REVIEWING);

        appeal.reject(200L);

        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.COMPLETED);
        assertThat(appeal.getReviewResult()).isEqualTo(ReviewResult.DISMISS);
    }

    /* ───── hold ───── */

    @Test
    @DisplayName("REVIEWING 상태에서 보류하면 상태가 유지된다")
    void hold_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.REVIEWING);

        appeal.hold();

        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.REVIEWING);
    }

    @Test
    @DisplayName("RECEIVING 상태에서 보류 시 예외가 발생한다")
    void hold_fail_notReviewing() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.RECEIVING);

        assertThatThrownBy(() -> appeal.hold())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("검토 중인 이의신청만 처리할 수 있습니다.");
    }
}
