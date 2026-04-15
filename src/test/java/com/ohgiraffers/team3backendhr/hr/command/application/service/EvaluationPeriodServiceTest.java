package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.EvaluationReferenceEventPublisher;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private AdminClient adminClient;

    @Mock
    private EvaluationReferenceEventPublisher evaluationReferenceEventPublisher;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private EvaluationPeriodCommandService service;

    private EvaluationPeriod buildPeriod(EvalPeriodStatus status) {
        return EvaluationPeriod.builder()
                .algorithmVersionId(1L)
                .evalYear(2026)
                .evalSequence(1)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("create succeeds")
    void create_success() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)
        );
        given(repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)).willReturn(false);
        given(repository.existsByEvalYearAndEvalSequence(2026, 1)).willReturn(false);
        given(repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                LocalDate.of(2026, 3, 31), LocalDate.of(2026, 1, 1))).willReturn(false);
        given(idGenerator.generate()).willReturn(123456L);
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() -> service.create(request));
        verify(repository).save(any(EvaluationPeriod.class));
        verify(qualitativeEvaluationService).createRecordsForPeriod(123456L);
    }

    @Test
    @DisplayName("create fails when end date is before start date")
    void create_fail_endDateBeforeStartDate() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1,
                LocalDate.of(2026, 3, 31), LocalDate.of(2026, 1, 1)
        );

        assertBusinessError(() -> service.create(request), ErrorCode.INVALID_DATE_RANGE);
    }

    @Test
    @DisplayName("create fails when year and sequence already exist")
    void create_fail_alreadyInProgress() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)
        );
        given(repository.existsByEvalYearAndEvalSequence(2026, 1)).willReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("동일한 연도·차수의 평가 기간이 이미 존재합니다.");
    }

    @Test
    @DisplayName("create fails when year and sequence already exist")
    void create_fail_duplicateSequence() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 1,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30)
        );
        given(repository.existsByEvalYearAndEvalSequence(2026, 1)).willReturn(true);

        assertBusinessError(() -> service.create(request), ErrorCode.EVAL_PERIOD_DUPLICATE);
    }

    @Test
    @DisplayName("create fails when date range overlaps")
    void create_fail_dateOverlap() {
        EvaluationPeriodCreateRequest request = new EvaluationPeriodCreateRequest(
                1L, 2026, 2,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31)
        );
        given(repository.existsByEvalYearAndEvalSequence(2026, 2)).willReturn(false);
        given(repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 3, 1))).willReturn(true);

        assertBusinessError(() -> service.create(request), ErrorCode.EVAL_PERIOD_DATE_OVERLAP);
    }

    @Test
    @DisplayName("update fails when date range overlaps")
    void update_fail_dateOverlap() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        given(repository.findById(any())).willReturn(Optional.of(period));

        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 5, 31),
                1L,
                null
        );
        given(repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndEvalPeriodIdNot(
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 3, 1), 1L)).willReturn(true);

        assertBusinessError(() -> service.update(1L, request), ErrorCode.EVAL_PERIOD_DATE_OVERLAP);
    }

    @Test
    @DisplayName("close succeeds")
    void close_success() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        given(repository.findById(any())).willReturn(Optional.of(period));

        service.close(1L);

        assertThat(period.getStatus()).isEqualTo(EvalPeriodStatus.CLOSING);
    }

    @Test
    @DisplayName("close fails when period is missing")
    void close_fail_notFound() {
        given(repository.findById(any())).willReturn(Optional.empty());

        assertBusinessError(() -> service.close(999L), ErrorCode.EVAL_PERIOD_NOT_FOUND);
    }

    @Test
    @DisplayName("confirm succeeds")
    void confirm_success() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.CLOSING);
        given(repository.findById(any())).willReturn(Optional.of(period));

        service.confirm(1L);

        assertThat(period.getStatus()).isEqualTo(EvalPeriodStatus.CONFIRMED);
    }

    @Test
    @DisplayName("update fails when end date is before start date")
    void update_fail_endDateBeforeStartDate() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        given(repository.findById(any())).willReturn(Optional.of(period));

        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 1, 1),
                1L,
                null
        );

        assertBusinessError(() -> service.update(1L, request), ErrorCode.INVALID_DATE_RANGE);
    }

    @Test
    @DisplayName("update fails when period is already confirmed")
    void update_fail_confirmed() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.CONFIRMED);
        given(repository.findById(any())).willReturn(Optional.of(period));

        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 4, 30),
                2L,
                null
        );

        assertBusinessError(() -> service.update(1L, request), ErrorCode.EVAL_PERIOD_ALREADY_CONFIRMED);
    }

    @Test
    @DisplayName("update keeps dates when request dates are null")
    void update_success_nullDatesKeepExisting() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        given(repository.findById(any())).willReturn(Optional.of(period));
        given(repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndEvalPeriodIdNot(
                period.getEndDate(), period.getStartDate(), 1L)).willReturn(false);

        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(null, null, 99L, null);

        assertThatNoException().isThrownBy(() -> service.update(1L, request));
        assertThat(period.getAlgorithmVersionId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("update succeeds")
    void update_success() {
        EvaluationPeriod period = buildPeriod(EvalPeriodStatus.IN_PROGRESS);
        given(repository.findById(any())).willReturn(Optional.of(period));
        given(repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndEvalPeriodIdNot(
                LocalDate.of(2026, 4, 30), LocalDate.of(2026, 2, 1), 1L)).willReturn(false);

        EvaluationPeriodUpdateRequest request = new EvaluationPeriodUpdateRequest(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 4, 30),
                2L,
                null
        );

        service.update(1L, request);

        assertThat(period.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(period.getEndDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(period.getAlgorithmVersionId()).isEqualTo(2L);
    }

    private void assertBusinessError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(errorCode));
    }
}
