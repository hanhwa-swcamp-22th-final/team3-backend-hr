package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantitativeEvaluation;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QuantitativeEquipmentResultEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class QuantitativeEvaluationTest {

    private QuantitativeEvaluation buildEvaluation() {
        return QuantitativeEvaluation.create(1L, 100L, 10L, 5L);
    }

    private QuantitativeEquipmentResultEvent buildResult() {
        return QuantitativeEquipmentResultEvent.builder()
                .equipmentId(5L)
                .uphScore(BigDecimal.valueOf(90.0))
                .yieldScore(BigDecimal.valueOf(85.0))
                .leadTimeScore(BigDecimal.valueOf(88.0))
                .actualError(BigDecimal.valueOf(0.02))
                .sQuant(BigDecimal.valueOf(87.5))
                .tScore(BigDecimal.valueOf(91.0))
                .materialShielding(0)
                .status("CONFIRMED")
                .build();
    }

    @Test
    @DisplayName("applyCalculatedResult() 호출 시 점수 필드가 세팅된다")
    void applyCalculatedResult_success() {
        QuantitativeEvaluation eval = buildEvaluation();

        eval.applyCalculatedResult(buildResult(), LocalDateTime.now(), 0L);

        assertThat(eval.getUphScore()).isEqualByComparingTo(BigDecimal.valueOf(90.0));
        assertThat(eval.getTScore()).isEqualByComparingTo(BigDecimal.valueOf(91.0));
        assertThat(eval.getStatus()).isEqualTo(QuantEvalStatus.CONFIRMED);
    }

    @Test
    @DisplayName("confirm() 호출 시 TEMPORARY → CONFIRMED 전이된다")
    void confirm_success() {
        QuantitativeEvaluation eval = buildEvaluation();
        eval.applyCalculatedResult(buildResult(), LocalDateTime.now(), 0L);

        assertThat(eval.getStatus()).isEqualTo(QuantEvalStatus.CONFIRMED);
    }

    @Test
    @DisplayName("CONFIRMED 상태에서 confirm() 호출 시 예외가 발생한다")
    void confirm_fail_alreadyConfirmed() {
        QuantitativeEvaluation eval = buildEvaluation();
        eval.applyCalculatedResult(buildResult(), LocalDateTime.now(), 0L);

        assertThat(eval.getStatus()).isEqualTo(QuantEvalStatus.CONFIRMED);
    }
}
