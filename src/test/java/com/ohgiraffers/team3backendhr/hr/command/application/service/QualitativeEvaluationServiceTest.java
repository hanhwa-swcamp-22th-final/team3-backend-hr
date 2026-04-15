package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationConfirmRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.InputMethod;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationCommentRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationAnalyzedEvent;
import java.math.BigDecimal;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.QualitativeEvaluationEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QualitativeEvaluationServiceTest {

    @Mock
    private QualitativeEvaluationRepository repository;

    @Mock
    private AdminClient adminClient;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private QualitativeEvaluationEventPublisher qualitativeEvaluationEventPublisher;

    @Mock
    private EvaluationPeriodRepository evaluationPeriodRepository;

    @Mock
    private EvaluationCommentRepository evaluationCommentRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QualitativeEvaluationCommandService service;

    private QualitativeEvaluation buildEval(QualEvalStatus status) {
        return QualitativeEvaluation.builder()
                .qualitativeEvaluationId(1L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .evaluationLevel(1L)
                .status(status)
                .build();
    }

    /* ── saveDraft ───────────────────────────────────────────────────────── */

    @Test
    @DisplayName("1차 평가 임시저장 성공 — NO_INPUT → DRAFT")
    void saveDraft_success() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(eval));

        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, "{\"TECHNICAL_COMPETENCE\": 80}", "임시저장 코멘트입니다.", InputMethod.TEXT);

        // when
        service.saveDraft(200L, 101L, request);

        // then
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.DRAFT);
        assertThat(eval.getEvaluatorId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("1차 평가 임시저장 — 레코드 없으면 예외")
    void saveDraft_fail_notFound() {
        // given
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.empty());

        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, null, null, InputMethod.TEXT);

        // when & then
        assertThatThrownBy(() -> service.saveDraft(200L, 101L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("평가를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("1차 평가 임시저장 — SUBMITTED 상태면 예외")
    void saveDraft_fail_alreadySubmitted() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.SUBMITTED);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(eval));

        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, null, null, InputMethod.TEXT);

        // when & then
        assertThatThrownBy(() -> service.saveDraft(200L, 101L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 제출된 평가입니다.");
    }

    /* ── submit ──────────────────────────────────────────────────────────── */

    @Test
    @DisplayName("1차 평가 제출 성공 — SUBMITTED 상태로 전환, score·grade는 null (batch 분석 전)")
    void submit_success() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.DRAFT);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(eval));
        given(evaluationPeriodRepository.findById(5L)).willReturn(Optional.of(
                EvaluationPeriod.builder().algorithmVersionId(1L).evalYear(2026).evalSequence(1)
                        .startDate(LocalDate.of(2026, 1, 1))
                        .endDate(LocalDate.of(2026, 3, 31)).status(EvalPeriodStatus.IN_PROGRESS).build()));

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, "{\"TECHNICAL_COMPETENCE\": 95}", "제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        // when
        service.submit(200L, 101L, request);

        // then
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.SUBMITTED);
        assertThat(eval.getScore()).isNull();
        assertThat(eval.getGrade()).isNull();
    }

    /* ── saveDraftForDL (2차) ────────────────────────────────────────────── */

    @Test
    @DisplayName("2차 평가 임시저장 성공 — level 2 레코드 DRAFT로 전환")
    void saveDraftForDL_success() {
        // given
        QualitativeEvaluation level1 = buildEval(QualEvalStatus.SUBMITTED);
        QualitativeEvaluation level2 = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(2L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .evaluationLevel(2L)
                .status(QualEvalStatus.NO_INPUT)
                .build();
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(level1));
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(2L))).willReturn(Optional.of(level2));

        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, "{\"LEADERSHIP\": 85}", "2차 임시저장 코멘트입니다.", InputMethod.TEXT);

        // when
        service.saveDraftForDL(300L, 101L, request);

        // then
        assertThat(level2.getStatus()).isEqualTo(QualEvalStatus.DRAFT);
        assertThat(level2.getEvaluatorId()).isEqualTo(300L);
    }

    @Test
    @DisplayName("2차 평가 임시저장 — 1차 평가가 SUBMITTED 아니면 예외")
    void saveDraftForDL_fail_level1NotSubmitted() {
        // given
        QualitativeEvaluation level1 = buildEval(QualEvalStatus.DRAFT);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(level1));

        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, null, null, InputMethod.TEXT);

        // when & then
        assertThatThrownBy(() -> service.saveDraftForDL(300L, 101L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("1차 평가가 제출되지 않아 2차 평가를 진행할 수 없습니다.");
    }

    @Test
    @DisplayName("2차 평가 임시저장 — level 2 레코드 없으면 예외")
    void saveDraftForDL_fail_notFound() {
        // given
        QualitativeEvaluation level1 = buildEval(QualEvalStatus.SUBMITTED);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(level1));
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(2L))).willReturn(Optional.empty());

        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, null, null, InputMethod.TEXT);

        // when & then
        assertThatThrownBy(() -> service.saveDraftForDL(300L, 101L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("평가를 찾을 수 없습니다.");
    }

    /* ── submitForDL (2차) ───────────────────────────────────────────────── */

    @Test
    @DisplayName("2차 평가 제출 성공 — SUBMITTED 상태로 전환, score·grade는 null (batch 분석 전)")
    void submitForDL_success() {
        // given
        QualitativeEvaluation level1 = buildEval(QualEvalStatus.SUBMITTED);
        QualitativeEvaluation level2 = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(2L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .evaluationLevel(2L)
                .status(QualEvalStatus.DRAFT)
                .build();
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(level1));
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(2L))).willReturn(Optional.of(level2));
        given(evaluationPeriodRepository.findById(5L)).willReturn(Optional.of(
                EvaluationPeriod.builder().algorithmVersionId(1L).evalYear(2026).evalSequence(1)
                        .startDate(LocalDate.of(2026, 1, 1))
                        .endDate(LocalDate.of(2026, 3, 31)).status(EvalPeriodStatus.IN_PROGRESS).build()));

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, "{\"LEADERSHIP\": 95}", "2차 제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        // when
        service.submitForDL(300L, 101L, request);

        // then
        assertThat(level2.getStatus()).isEqualTo(QualEvalStatus.SUBMITTED);
        assertThat(level2.getScore()).isNull();
        assertThat(level2.getGrade()).isNull();
    }

    @Test
    @DisplayName("2차 평가 제출 — 1차 평가가 SUBMITTED 아니면 예외")
    void submitForDL_fail_level1NotSubmitted() {
        // given
        QualitativeEvaluation level1 = buildEval(QualEvalStatus.NO_INPUT);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(level1));

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "2차 제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        // when & then
        assertThatThrownBy(() -> service.submitForDL(300L, 101L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("1차 평가가 제출되지 않아 2차 평가를 진행할 수 없습니다.");
    }

    @Test
    @DisplayName("2차 평가 제출 — level 2 레코드 없으면 예외")
    void submitForDL_fail_notFound() {
        // given
        QualitativeEvaluation level1 = buildEval(QualEvalStatus.SUBMITTED);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(level1));
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(2L))).willReturn(Optional.empty());

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "2차 제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        // when & then
        assertThatThrownBy(() -> service.submitForDL(300L, 101L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("평가를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("1차 평가 제출 — 레코드 없으면 예외")
    void submit_fail_notFound() {
        // given
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.empty());

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        // when & then
        assertThatThrownBy(() -> service.submit(200L, 101L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("평가를 찾을 수 없습니다.");
    }

    /* ── createRecordsForPeriod ──────────────────────────────────────────── */

    @Test
    @DisplayName("평가 기간 생성 시 WORKER × level 1·2·3 레코드를 선생성한다")
    void createRecordsForPeriod_success() {
        // given
        WorkerResponse w1 = new WorkerResponse(); w1.setEmployeeId(101L);
        WorkerResponse w2 = new WorkerResponse(); w2.setEmployeeId(102L);
        given(adminClient.getWorkers()).willReturn(List.of(w1, w2));
        given(idGenerator.generate()).willReturn(1L, 2L, 3L, 4L, 5L, 6L);
        given(repository.saveAll(any())).willReturn(List.of());

        // when
        service.createRecordsForPeriod(10L);

        // then — WORKER 2명 × level 3 = 6개
        verify(repository).saveAll(argThat(list -> ((List<?>) list).size() == 6));
    }

    /* ── confirmFinal (3차 HRM) ─────────────────────────────────────────── */

    @Test
    @DisplayName("3차 최종 확정 성공 — level 2 SUBMITTED이면 level 3 CONFIRMED")
    void confirmFinal_success() {
        // given
        QualitativeEvaluation level2 = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(2L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .evaluationLevel(2L)
                .status(QualEvalStatus.SUBMITTED)
                .build();
        QualitativeEvaluation level3 = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(3L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .evaluationLevel(3L)
                .status(QualEvalStatus.NO_INPUT)
                .build();
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(2L))).willReturn(Optional.of(level2));
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(3L))).willReturn(Optional.of(level3));

        QualitativeEvaluationConfirmRequest request = new QualitativeEvaluationConfirmRequest(
                5L, "3차 최종 확정 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        // when
        service.confirmFinal(400L, 101L, request);

        // then
        assertThat(level3.getStatus()).isEqualTo(QualEvalStatus.CONFIRMED);
        assertThat(level3.getEvaluatorId()).isEqualTo(400L);
    }

    @Test
    @DisplayName("3차 최종 확정 — 2차 평가가 SUBMITTED 아니면 예외")
    void confirmFinal_fail_level2NotSubmitted() {
        // given
        QualitativeEvaluation level2 = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(2L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .evaluationLevel(2L)
                .status(QualEvalStatus.DRAFT)
                .build();
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(2L))).willReturn(Optional.of(level2));

        QualitativeEvaluationConfirmRequest request = new QualitativeEvaluationConfirmRequest(
                5L, "3차 최종 확정 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT);

        // when & then
        assertThatThrownBy(() -> service.confirmFinal(400L, 101L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("2차 평가가 제출되지 않아 최종 확정을 진행할 수 없습니다.");
    }

    @Test
    @DisplayName("3차 최종 확정 — 코멘트 20자 미만이면 예외")
    void confirmFinal_fail_commentTooShort() {
        // given
        QualitativeEvaluation level2 = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(2L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .evaluationLevel(2L)
                .status(QualEvalStatus.SUBMITTED)
                .build();
        QualitativeEvaluation level3 = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(3L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .evaluationLevel(3L)
                .status(QualEvalStatus.NO_INPUT)
                .build();
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(2L))).willReturn(Optional.of(level2));
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(3L))).willReturn(Optional.of(level3));

        QualitativeEvaluationConfirmRequest request = new QualitativeEvaluationConfirmRequest(
                5L, "짧은코멘트", InputMethod.TEXT);

        // when & then
        assertThatThrownBy(() -> service.confirmFinal(400L, 101L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("평가 코멘트는 최소 20자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("1차 평가 제출 — 코멘트 20자 미만이면 예외")
    void submit_fail_commentTooShort() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.DRAFT);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(eval));

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "짧은코멘트", InputMethod.TEXT);

        // when & then
        assertThatThrownBy(() -> service.submit(200L, 101L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("평가 코멘트는 최소 20자 이상이어야 합니다.");
    }

    /* ── applyAnalysisResult (batch NLP 분석 결과 반영) ─────────────────────── */

    @Test
    @DisplayName("batch 분석 결과 반영 성공 — score가 세팅된다")
    void applyAnalysisResult_success() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.SUBMITTED);
        QualitativeEvaluationAnalyzedEvent event = new QualitativeEvaluationAnalyzedEvent();
        event.setQualitativeEvaluationId(1L);
        event.setRawScore(BigDecimal.valueOf(88.0));
        given(repository.findById(1L)).willReturn(Optional.of(eval));

        // when
        service.applyAnalyzedResult(event);

        // then
        assertThat(eval.getScore()).isEqualTo(88.0);
    }

    @Test
    @DisplayName("batch 분석 결과 반영 — 평가 레코드 없으면 예외")
    void applyAnalysisResult_fail_notFound() {
        // given
        QualitativeEvaluationAnalyzedEvent event = new QualitativeEvaluationAnalyzedEvent();
        event.setQualitativeEvaluationId(999L);
        event.setRawScore(BigDecimal.valueOf(88.0));
        given(repository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.applyAnalyzedResult(event))
                .isInstanceOf(BusinessException.class)
                .hasMessage("평가를 찾을 수 없습니다.");
    }
}
