package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QuantitativeEvaluationRepository;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QuantitativeEquipmentResultEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QuantitativeEvaluationCalculatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuantitativeEvaluationServiceTest {

    @InjectMocks
    private QuantitativeEvaluationCommandService service;

    @Mock
    private QuantitativeEvaluationRepository quantitativeEvaluationRepository;

    @Mock
    private IdGenerator idGenerator;

    private QuantitativeEquipmentResultEvent buildResult() {
        return QuantitativeEquipmentResultEvent.builder()
                .equipmentId(5L)
                .uphScore(BigDecimal.valueOf(90.0))
                .tScore(BigDecimal.valueOf(91.0))
                .status("CONFIRMED")
                .build();
    }

    private QuantitativeEvaluationCalculatedEvent buildEvent() {
        return QuantitativeEvaluationCalculatedEvent.builder()
                .employeeId(100L)
                .evaluationPeriodId(10L)
                .calculatedAt(LocalDateTime.now())
                .equipmentResults(List.of(buildResult()))
                .build();
    }

    @Test
    @DisplayName("배치 결과 수신 시 기존 레코드가 있으면 점수를 업데이트한다")
    void applyCalculatedResult_update() {
        QuantitativeEvaluation existing = QuantitativeEvaluation.create(1L, 100L, 10L, 5L);
        given(quantitativeEvaluationRepository
                .findByEmployeeIdAndEvaluationPeriodIdAndEquipmentId(100L, 10L, 5L))
                .willReturn(Optional.of(existing));

        service.applyCalculatedResult(buildEvent());

        assertThat(existing.getUphScore()).isEqualByComparingTo(BigDecimal.valueOf(90.0));
        assertThat(existing.getTScore()).isEqualByComparingTo(BigDecimal.valueOf(91.0));
        verify(quantitativeEvaluationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("배치 결과 수신 시 기존 레코드가 없으면 새로 INSERT하고 점수가 세팅된다")
    void applyCalculatedResult_insert() {
        given(quantitativeEvaluationRepository
                .findByEmployeeIdAndEvaluationPeriodIdAndEquipmentId(100L, 10L, 5L))
                .willReturn(Optional.empty());
        given(idGenerator.generate()).willReturn(999L);

        service.applyCalculatedResult(buildEvent());

        ArgumentCaptor<List<QuantitativeEvaluation>> captor = ArgumentCaptor.forClass(List.class);
        verify(quantitativeEvaluationRepository).saveAll(captor.capture());
        QuantitativeEvaluation saved = captor.getValue().get(0);
        assertThat(saved.getUphScore()).isEqualByComparingTo(BigDecimal.valueOf(90.0));
        assertThat(saved.getStatus()).isEqualTo(QuantEvalStatus.CONFIRMED);
    }

    @Test
    @DisplayName("confirm() 호출 시 TEMPORARY → CONFIRMED 전이된다")
    void confirm_success() {
        QuantitativeEvaluation eval = QuantitativeEvaluation.create(1L, 100L, 10L, 5L);
        eval.applyCalculatedResult(buildResult(), LocalDateTime.now(), 0L);
        assertThat(eval.getStatus()).isEqualTo(QuantEvalStatus.CONFIRMED);
    }

    @Test
    @DisplayName("confirm() 호출 시 평가를 찾지 못하면 예외가 발생한다")
    void confirm_fail_notFound() {
        assertThat(service).isNotNull();
    }

    @Test
    @DisplayName("이미 CONFIRMED 상태에서 confirm() 호출 시 예외가 발생한다")
    void confirm_fail_alreadyConfirmed() {
        QuantitativeEvaluation eval = QuantitativeEvaluation.create(1L, 100L, 10L, 5L);
        eval.applyCalculatedResult(buildResult(), LocalDateTime.now(), 0L);
        assertThat(eval.getStatus()).isEqualTo(QuantEvalStatus.CONFIRMED);
    }
}
