package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantitativeEvaluation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuantitativeEvaluationTest {

    private QuantitativeEvaluation buildEvaluation(QuantEvalStatus status) {
        return QuantitativeEvaluation.builder()
                .quantitativeEvaluationId(1L)
                .employeeId(100L)
                .evalPeriodId(10L)
                .equipmentId(5L)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("TEMPORARY 상태에서 confirm() 호출 시 CONFIRMED로 전이된다")
    void confirm_success() {
        QuantitativeEvaluation eval = buildEvaluation(QuantEvalStatus.TEMPORARY);

        eval.confirm();

        assertThat(eval.getStatus()).isEqualTo(QuantEvalStatus.CONFIRMED);
    }

    @Test
    @DisplayName("이미 CONFIRMED 상태에서 confirm() 호출 시 예외가 발생한다")
    void confirm_fail_alreadyConfirmed() {
        QuantitativeEvaluation eval = buildEvaluation(QuantEvalStatus.CONFIRMED);

        assertThatThrownBy(eval::confirm)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.EVALUATION_ALREADY_CONFIRMED.getMessage());
    }

    @Test
    @DisplayName("applyBatchResult() 호출 시 점수 필드가 세팅된다")
    void applyBatchResult_success() {
        QuantitativeEvaluation eval = buildEvaluation(QuantEvalStatus.TEMPORARY);

        eval.applyBatchResult(90.0, 85.0, 88.0, 0.02, 87.5, 91.0, true);

        assertThat(eval.getUphScore()).isEqualTo(90.0);
        assertThat(eval.getYieldScore()).isEqualTo(85.0);
        assertThat(eval.getLeadTimeScore()).isEqualTo(88.0);
        assertThat(eval.getActualError()).isEqualTo(0.02);
        assertThat(eval.getSQuant()).isEqualTo(87.5);
        assertThat(eval.getTScore()).isEqualTo(91.0);
        assertThat(eval.getMaterialShielding()).isTrue();
        assertThat(eval.getStatus()).isEqualTo(QuantEvalStatus.TEMPORARY);
    }

    @Test
    @DisplayName("CONFIRMED 상태에서 applyBatchResult() 호출 시 예외가 발생한다")
    void applyBatchResult_fail_alreadyConfirmed() {
        QuantitativeEvaluation eval = buildEvaluation(QuantEvalStatus.CONFIRMED);

        assertThatThrownBy(() -> eval.applyBatchResult(90.0, 85.0, 88.0, 0.02, 87.5, 91.0, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.EVALUATION_ALREADY_CONFIRMED.getMessage());
    }
}
