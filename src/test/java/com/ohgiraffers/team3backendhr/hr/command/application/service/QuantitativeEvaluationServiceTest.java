package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantitativeEvaluation;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QuantitativeEvaluationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuantitativeEvaluationServiceTest {

    @InjectMocks
    private QuantitativeEvaluationCommandService service;

    @Mock
    private QuantitativeEvaluationRepository repository;

    @Mock
    private IdGenerator idGenerator;

    private QuantitativeEvaluation buildEval(QuantEvalStatus status) {
        return QuantitativeEvaluation.builder()
                .quantitativeEvaluationId(1L)
                .employeeId(100L)
                .evalPeriodId(10L)
                .equipmentId(5L)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("배치 결과 수신 시 기존 레코드가 있으면 점수를 업데이트한다")
    void applyBatchResult_update() {
        QuantitativeEvaluation existing = buildEval(QuantEvalStatus.TEMPORARY);
        given(repository.findByEmployeeIdAndEvalPeriodId(100L, 10L)).willReturn(Optional.of(existing));

        service.applyBatchResult(100L, 10L, 5L, 90.0, 85.0, 88.0, 0.02, 87.5, 91.0, true);

        assertThat(existing.getUphScore()).isEqualTo(90.0);
        assertThat(existing.getTScore()).isEqualTo(91.0);
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("배치 결과 수신 시 기존 레코드가 없으면 새로 INSERT한다")
    void applyBatchResult_insert() {
        given(repository.findByEmployeeIdAndEvalPeriodId(100L, 10L)).willReturn(Optional.empty());
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        service.applyBatchResult(100L, 10L, 5L, 90.0, 85.0, 88.0, 0.02, 87.5, 91.0, true);

        verify(repository).save(any(QuantitativeEvaluation.class));
    }

    @Test
    @DisplayName("confirm() 호출 시 TEMPORARY → CONFIRMED 전이된다")
    void confirm_success() {
        QuantitativeEvaluation eval = buildEval(QuantEvalStatus.TEMPORARY);
        given(repository.findById(1L)).willReturn(Optional.of(eval));

        service.confirm(1L);

        assertThat(eval.getStatus()).isEqualTo(QuantEvalStatus.CONFIRMED);
        verify(repository).save(eval);
    }

    @Test
    @DisplayName("confirm() 호출 시 평가를 찾지 못하면 예외가 발생한다")
    void confirm_fail_notFound() {
        given(repository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirm(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.EVALUATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 CONFIRMED 상태에서 confirm() 호출 시 예외가 발생한다")
    void confirm_fail_alreadyConfirmed() {
        QuantitativeEvaluation eval = buildEval(QuantEvalStatus.CONFIRMED);
        given(repository.findById(1L)).willReturn(Optional.of(eval));

        assertThatThrownBy(() -> service.confirm(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.EVALUATION_ALREADY_CONFIRMED.getMessage());
    }
}
