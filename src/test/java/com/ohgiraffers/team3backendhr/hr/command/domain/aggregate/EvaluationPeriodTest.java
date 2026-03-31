package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class EvaluationPeriodTest {

    private EvaluationPeriod buildPeriod(EvalPeriodStatus status) {
        return EvaluationPeriod.builder()
                .algorithmVersionId(1L)
                .evalYear(2026)
                .evalSequence(1)
                .evalType(EvalType.QUALITATIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("IN_PROGRESS 상태에서 close() 호출 시 CLOSING으로 전이된다")
    void close_success() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);

        period.close();

        assertThat(period.getStatus()).isEqualTo(EvalPeriodStatus.CLOSING);
    }

    @Test
    @DisplayName("IN_PROGRESS가 아닌 상태에서 close() 호출 시 예외가 발생한다")
    void close_fail_when_not_in_progress() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.CONFIRMED);

        assertThatThrownBy(period::close)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("진행 중인 평가 기간만 마감할 수 있습니다.");
    }

    @Test
    @DisplayName("CLOSING 상태에서 confirm() 호출 시 CONFIRMED로 전이된다")
    void confirm_success() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.CLOSING);

        period.confirm();

        assertThat(period.getStatus()).isEqualTo(EvalPeriodStatus.CONFIRMED);
    }

    @Test
    @DisplayName("CLOSING이 아닌 상태에서 confirm() 호출 시 예외가 발생한다")
    void confirm_fail_when_not_closing() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);

        assertThatThrownBy(period::confirm)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("마감된 평가 기간만 확정할 수 있습니다.");
    }

    @Test
    @DisplayName("update() 호출 시 전달된 값만 변경된다")
    void update_success() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        LocalDate newEndDate = LocalDate.of(2026, 4, 30);

        period.update(null, newEndDate, null);

        assertThat(period.getEndDate()).isEqualTo(newEndDate);
        assertThat(period.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    }
}
