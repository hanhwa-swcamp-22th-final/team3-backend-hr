package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodUpdateRequest;
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
    private QualitativeEvaluationCommandService qualitativeEvaluationService;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private EvaluationPeriodCommandService service;

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
    @DisplayName("평가 기간을 생성하고 Admin에서 WORKER 조회 후 level 1·2·3 레코드를 선생성한다")
    void create_success() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1, EvalType.QUALITATIVE,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)
        );
        given(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).willReturn(false);
        given(idGenerator.generate()).willReturn(123456L);
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() -> service.create(request));
        verify(repository).save(any(EvaluationPeriod.class));
        verify(qualitativeEvaluationService).createRecordsForPeriod(123456L);
    }

    @Test
    @DisplayName("종료일이 시작일보다 이전이면 생성 시 예외가 발생한다")
    void create_fail_endDateBeforeStartDate() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1, EvalType.QUALITATIVE,
                LocalDate.of(2026, 3, 31), LocalDate.of(2026, 1, 1)
        );
        given(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).willReturn(false);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료일은 시작일보다 이후여야 합니다.");
    }

    @Test
    @DisplayName("이미 진행 중인 평가 기간이 있으면 생성 시 예외가 발생한다")
    void create_fail_alreadyInProgress() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1, EvalType.QUALITATIVE,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)
        );
        given(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).willReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 진행 중인 평가 기간이 있습니다.");
    }

    @Test
    @DisplayName("동일한 연도·차수·유형의 평가 기간이 이미 존재하면 생성 시 예외가 발생한다")
    void create_fail_duplicateSequence() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1, EvalType.QUALITATIVE,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30)
        );
        given(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).willReturn(false);
        given(repository.existsByEvalYearAndEvalSequenceAndEvalType(2026, 1, EvalType.QUALITATIVE)).willReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("동일한 연도·차수·평가 유형의 평가 기간이 이미 존재합니다.");
    }

    @Test
    @DisplayName("날짜가 기존 평가 기간과 겹치면 생성 시 예외가 발생한다")
    void create_fail_dateOverlap() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 2, EvalType.QUALITATIVE,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31)
        );
        given(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).willReturn(false);
        given(repository.existsByEvalYearAndEvalSequenceAndEvalType(2026, 2, EvalType.QUALITATIVE)).willReturn(false);
        given(repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 3, 1))).willReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("기존 평가 기간과 날짜가 중복됩니다.");
    }

    @Test
    @DisplayName("수정 시 날짜가 다른 평가 기간과 겹치면 예외가 발생한다")
    void update_fail_dateOverlap() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        given(repository.findById(any())).willReturn(Optional.of(period));

        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 5, 31),
                1L
        );
        given(repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndEvalPeriodIdNot(
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 3, 1), 1L)).willReturn(true);

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("기존 평가 기간과 날짜가 중복됩니다.");
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
    @DisplayName("수정 시 종료일이 시작일보다 이전이면 예외가 발생한다")
    void update_fail_endDateBeforeStartDate() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        given(repository.findById(any())).willReturn(Optional.of(period));

        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 1, 1),
                1L
        );

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료일은 시작일보다 이후여야 합니다.");
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
    @DisplayName("날짜를 전달하지 않으면 기존 날짜를 유지한 채 algorithmVersionId만 수정한다")
    void update_success_nullDatesKeepExisting() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        given(repository.findById(any())).willReturn(Optional.of(period));
        given(repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndEvalPeriodIdNot(
                period.getEndDate(), period.getStartDate(), 1L)).willReturn(false);

        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(null, null, 99L);

        assertThatNoException().isThrownBy(() -> service.update(1L, request));
        assertThat(period.getAlgorithmVersionId()).isEqualTo(99L);
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
