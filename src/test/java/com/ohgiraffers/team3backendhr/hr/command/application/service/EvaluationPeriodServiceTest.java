package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.EvaluationPeriodUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EvaluationPeriodServiceTest {

    @Mock
    private EvaluationPeriodRepository repository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private EvaluationPeriodService service;

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
    @DisplayName("평가 기간을 생성한다")
    void create_success() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L,
                2026,
                1,
                EvalType.QUALITATIVE,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );
        given(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).willReturn(false);
        given(idGenerator.generate()).willReturn(123456L);
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() -> service.create(request));
        verify(idGenerator).generate();
        verify(repository).save(any(EvaluationPeriod.class));
    }

    @Test
    @DisplayName("이미 진행 중인 평가 기간이 있으면 생성 시 예외가 발생한다")
    void create_fail_alreadyInProgress() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L,
                2026,
                1,
                EvalType.QUALITATIVE,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );
        given(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).willReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 진행 중인 평가 기간이 있습니다.");
    }

    @Test
    @DisplayName("평가 기간을 마감한다")
    void close_success() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        given(repository.findById(any())).willReturn(Optional.of(period));

        service.close(1L);

        assertThat(period.getStatus()).isEqualTo(EvalPeriodStatus.CLOSING);
    }

    @Test
    @DisplayName("존재하지 않는 평가 기간 마감 시 예외가 발생한다")
    void close_fail_notFound() {
        given(repository.findById(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.close(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평가 기간을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("평가 기간을 확정한다")
    void confirm_success() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.CLOSING);
        given(repository.findById(any())).willReturn(Optional.of(period));

        service.confirm(1L);

        assertThat(period.getStatus()).isEqualTo(EvalPeriodStatus.CONFIRMED);
    }

    @Test
    @DisplayName("확정된 평가 기간 수정 시 예외가 발생한다")
    void update_fail_confirmed() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.CONFIRMED);
        given(repository.findById(any())).willReturn(Optional.of(period));

        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 4, 30),
                2L
        );

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("확정된 평가 기간은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("평가 기간을 수정한다")
    void update_success() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        given(repository.findById(any())).willReturn(Optional.of(period));

        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 4, 30),
                2L
        );

        service.update(1L, request);

        assertThat(period.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(period.getEndDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(period.getAlgorithmVersionId()).isEqualTo(2L);
    }
}
