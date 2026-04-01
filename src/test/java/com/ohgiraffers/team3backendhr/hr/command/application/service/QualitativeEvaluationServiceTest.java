package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.InputMethod;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QualitativeEvaluationServiceTest {

    @Mock
    private QualitativeEvaluationRepository repository;

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
        QualitativeEvaluation eval = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(2L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .evaluationLevel(2L)
                .status(QualEvalStatus.NO_INPUT)
                .build();
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(2L))).willReturn(Optional.of(eval));

        QualitativeEvaluationDraftRequest request = new QualitativeEvaluationDraftRequest(
                5L, "{\"LEADERSHIP\": 85}", "2차 임시저장 코멘트입니다.", InputMethod.TEXT);

        // when
        service.saveDraftForDL(300L, 101L, request);

        // then
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.DRAFT);
        assertThat(eval.getEvaluatorId()).isEqualTo(300L);
    }

    @Test
    @DisplayName("2차 평가 임시저장 — level 2 레코드 없으면 예외")
    void saveDraftForDL_fail_notFound() {
        // given
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
        QualitativeEvaluation eval = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(2L)
                .evaluateeId(101L)
                .evaluationPeriodId(5L)
                .evaluationLevel(2L)
                .status(QualEvalStatus.DRAFT)
                .build();
        given(repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                eq(101L), eq(5L), eq(2L))).willReturn(Optional.of(eval));

        QualitativeEvaluationSubmitRequest request = new QualitativeEvaluationSubmitRequest(
                5L, "{\"LEADERSHIP\": 95}", "2차 제출 코멘트입니다. 충분히 길게 작성했습니다.", InputMethod.TEXT, 91.0);

        // when
        service.submitForDL(300L, 101L, request);

        // then
        assertThat(eval.getStatus()).isEqualTo(QualEvalStatus.SUBMITTED);
        assertThat(eval.getGrade()).isEqualTo(Grade.S);
        assertThat(eval.getScore()).isEqualTo(91.0);
    }

    @Test
    @DisplayName("2차 평가 제출 — level 2 레코드 없으면 예외")
    void submitForDL_fail_notFound() {
        // given
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
