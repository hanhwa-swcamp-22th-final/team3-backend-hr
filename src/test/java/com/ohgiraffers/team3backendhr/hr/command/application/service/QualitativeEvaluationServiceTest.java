package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationConfirmRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.InputMethod;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
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

    @InjectMocks
    private QualitativeEvaluationService service;

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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평가 레코드를 찾을 수 없습니다.");
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
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 제출된 평가는 수정할 수 없습니다.");
    }

    /* ── submit ──────────────────────────────────────────────────────────── */

    @Test
    @DisplayName("1차 평가 제출 성공 — score 90 이상이면 grade S")
    void submit_success_gradeS() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.DRAFT);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(eval));

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, "{\"TECHNICAL_COMPETENCE\": 95}", "제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT, 92.0);

        // when
        service.submit(200L, 101L, request);

        // then
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.SUBMITTED);
        assertThat(eval.getGrade()).isEqualTo(Grade.S);
        assertThat(eval.getScore()).isEqualTo(92.0);
    }

    @Test
    @DisplayName("1차 평가 제출 성공 — score 80 이상이면 grade A")
    void submit_success_gradeA() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(eval));

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT, 85.0);

        // when
        service.submit(200L, 101L, request);

        // then
        assertThat(eval.getGrade()).isEqualTo(Grade.A);
    }

    @Test
    @DisplayName("1차 평가 제출 성공 — score 70 이상이면 grade B")
    void submit_success_gradeB() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(eval));

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT, 75.0);

        // when
        service.submit(200L, 101L, request);

        // then
        assertThat(eval.getGrade()).isEqualTo(Grade.B);
    }

    @Test
    @DisplayName("1차 평가 제출 성공 — score 70 미만이면 grade C")
    void submit_success_gradeC() {
        // given
        QualitativeEvaluation eval = buildEval(QualEvalStatus.NO_INPUT);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(eval));

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT, 60.0);

        // when
        service.submit(200L, 101L, request);

        // then
        assertThat(eval.getGrade()).isEqualTo(Grade.C);
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
                .isInstanceOf(IllegalStateException.class)
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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평가 레코드를 찾을 수 없습니다.");
    }

    /* ── submitForDL (2차) ───────────────────────────────────────────────── */

    @Test
    @DisplayName("2차 평가 제출 성공 — score 90 이상이면 grade S")
    void submitForDL_success_gradeS() {
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

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, "{\"LEADERSHIP\": 95}", "2차 제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT, 91.0);

        // when
        service.submitForDL(300L, 101L, request);

        // then
        assertThat(level2.getStatus()).isEqualTo(QualEvalStatus.SUBMITTED);
        assertThat(level2.getGrade()).isEqualTo(Grade.S);
        assertThat(level2.getScore()).isEqualTo(91.0);
    }

    @Test
    @DisplayName("2차 평가 제출 — 1차 평가가 SUBMITTED 아니면 예외")
    void submitForDL_fail_level1NotSubmitted() {
        // given
        QualitativeEvaluation level1 = buildEval(QualEvalStatus.NO_INPUT);
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.of(level1));

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "2차 제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT, 88.0);

        // when & then
        assertThatThrownBy(() -> service.submitForDL(300L, 101L, request))
                .isInstanceOf(IllegalStateException.class)
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
                5L, null, "2차 제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT, 88.0);

        // when & then
        assertThatThrownBy(() -> service.submitForDL(300L, 101L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평가 레코드를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("1차 평가 제출 — 레코드 없으면 예외")
    void submit_fail_notFound() {
        // given
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(1L))).willReturn(Optional.empty());

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, null, "제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT, 85.0);

        // when & then
        assertThatThrownBy(() -> service.submit(200L, 101L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평가 레코드를 찾을 수 없습니다.");
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
                .isInstanceOf(IllegalStateException.class)
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
                .isInstanceOf(IllegalArgumentException.class)
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
                5L, null, "짧은코멘트", InputMethod.TEXT, 85.0);

        // when & then
        assertThatThrownBy(() -> service.submit(200L, 101L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평가 코멘트는 최소 20자 이상이어야 합니다.");
    }
}
