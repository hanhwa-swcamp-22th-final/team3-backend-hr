package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationTargetItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationTargetResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationGradeSummaryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.EvaluationSummaryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.TlEvaluationTargetItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.TlEvaluationTargetResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QualitativeEvaluationQueryMapper;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QualitativeEvaluationQueryServiceTest {

    @Mock
    private QualitativeEvaluationQueryMapper mapper;

    @Mock
    private EvaluationPeriodRepository evaluationPeriodRepository;

    @Mock
    private AdminClient adminClient;

    @InjectMocks
    private QualitativeEvaluationQueryService service;

    private EvaluationPeriod buildPeriod(Long id) {
        return EvaluationPeriod.builder()
                .evalPeriodId(id)
                .algorithmVersionId(1L)
                .evalYear(2026)
                .evalSequence(1)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .build();
    }

    /* ── getTlTargets ─────────────────────────────────────────────────── */

    @Test
    @DisplayName("TL 평가 대상 조회 — periodId 전달 시 그대로 사용")
    void getTlTargets_withPeriodId_returnsList() {
        // given
        TlEvaluationTargetItem item = new TlEvaluationTargetItem();
        item.setEvaluateeId(101L);
        given(mapper.findTlTargets(200L, 5L)).willReturn(List.of(item));
        given(evaluationPeriodRepository.findById(5L)).willReturn(Optional.of(buildPeriod(5L)));

        // when
        TlEvaluationTargetResponse result = service.getTlTargets(200L, 5L);

        // then
        assertThat(result.getEvalPeriodId()).isEqualTo(5L);
        assertThat(result.getTargets()).hasSize(1);
    }

    @Test
    @DisplayName("TL 평가 대상 조회 — periodId null 이면 현재 기간으로 자동 resolve")
    void getTlTargets_withNullPeriodId_resolvesToCurrent() {
        // given
        given(mapper.findCurrentPeriodId()).willReturn(10L);
        given(mapper.findTlTargets(200L, 10L)).willReturn(List.of());
        given(evaluationPeriodRepository.findById(10L)).willReturn(Optional.of(buildPeriod(10L)));

        // when
        TlEvaluationTargetResponse result = service.getTlTargets(200L, null);

        // then
        assertThat(result.getEvalPeriodId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("TL 평가 대상 조회 — periodId null이고 현재 기간 없으면 예외")
    void getTlTargets_noCurrentPeriod_throwsException() {
        // given
        given(mapper.findCurrentPeriodId()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> service.getTlTargets(200L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("현재 진행 중인 평가 기간이 없습니다.");
    }

    /* ── getDlTargets ─────────────────────────────────────────────────── */

    @Test
    @DisplayName("DL 평가 대상 조회 — periodId 전달 시 그대로 사용")
    void getDlTargets_withPeriodId_returnsList() {
        // given
        DlEvaluationTargetItem item = new DlEvaluationTargetItem();
        item.setEvaluateeId(101L);
        item.setFirstStageScore(85.0);
        given(mapper.findDlTargets(300L, 5L)).willReturn(List.of(item));
        given(evaluationPeriodRepository.findById(5L)).willReturn(Optional.of(buildPeriod(5L)));

        // when
        DlEvaluationTargetResponse result = service.getDlTargets(300L, 5L);

        // then
        assertThat(result.getEvalPeriodId()).isEqualTo(5L);
        assertThat(result.getTargets()).hasSize(1);
    }

    @Test
    @DisplayName("DL 평가 대상 조회 — periodId null이면 현재 기간으로 자동 resolve")
    void getDlTargets_withNullPeriodId_resolvesToCurrent() {
        // given
        given(mapper.findCurrentPeriodId()).willReturn(10L);
        given(mapper.findDlTargets(300L, 10L)).willReturn(List.of());
        given(evaluationPeriodRepository.findById(10L)).willReturn(Optional.of(buildPeriod(10L)));

        // when
        DlEvaluationTargetResponse result = service.getDlTargets(300L, null);

        // then
        assertThat(result.getEvalPeriodId()).isEqualTo(10L);
    }

    /* ── getEvaluations (HRM) ─────────────────────────────────────────── */

    @Test
    @DisplayName("HRM 평가 목록 조회 — 정상 반환 및 페이징 계산")
    void getEvaluations_returnsPaginatedList() {
        // given
        EvaluationSummaryItem item = new EvaluationSummaryItem();
        item.setEvalId(1L);
        given(mapper.findEvaluations(5L, null, null, 10, 10)).willReturn(List.of(item));
        given(mapper.countEvaluations(5L, null, null)).willReturn(25L);

        // when
        EvaluationListResponse result = service.getEvaluations(5L, null, null, 1, 10);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(25L);
        assertThat(result.getTotalPages()).isEqualTo(3L);
    }

    @Test
    @DisplayName("HRM 평가 목록 조회 — periodId null이면 현재 기간으로 자동 resolve")
    void getEvaluations_withNullPeriodId_resolvesToCurrent() {
        // given
        given(mapper.findCurrentPeriodId()).willReturn(7L);
        given(mapper.findEvaluations(7L, null, null, 10, 10)).willReturn(List.of());
        given(mapper.countEvaluations(7L, null, null)).willReturn(0L);

        // when
        EvaluationListResponse result = service.getEvaluations(null, null, null, 1, 10);

        // then
        assertThat(result.getTotalCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("HRM 평가 목록 조회 — periodId null이고 현재 기간 없으면 예외")
    void getEvaluations_noCurrentPeriod_throwsException() {
        // given
        given(mapper.findCurrentPeriodId()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> service.getEvaluations(null, null, null, 1, 10))
                .isInstanceOf(BusinessException.class)
                .hasMessage("현재 진행 중인 평가 기간이 없습니다.");
    }

    /* ── getEvaluationDetail (HRM) ────────────────────────────────────── */

    @Test
    @DisplayName("HRM 평가 상세 조회 — 정상 반환")
    void getEvaluationDetail_success() {
        // given
        EvaluationDetailResponse detail = new EvaluationDetailResponse();
        detail.setEvalId(1L);
        given(mapper.findEvaluationDetail(1L)).willReturn(detail);

        // when
        EvaluationDetailResponse result = service.getEvaluationDetail(1L);

        // then
        assertThat(result.getEvalId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("HRM 평가 상세 조회 — 존재하지 않으면 예외")
    void getEvaluationDetail_notFound_throwsException() {
        // given
        given(mapper.findEvaluationDetail(99L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> service.getEvaluationDetail(99L))
                .isInstanceOf(BusinessException.class);
    }

    /* ── getEvaluationGradeSummary (HRM) ─────────────────────────────── */

    /* ── getTlEvaluationDetail (TL) ───────────────────────────────────── */

    @Test
    @DisplayName("TL 제출 완료 평가 상세 조회 — 정상 반환")
    void getTlEvaluationDetail_success() {
        // given
        EvaluationDetailResponse detail = new EvaluationDetailResponse();
        detail.setEvalId(1L);
        given(mapper.findTlEvaluationDetail(1L, 200L)).willReturn(detail);

        // when
        EvaluationDetailResponse result = service.getTlEvaluationDetail(200L, 1L);

        // then
        assertThat(result.getEvalId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("TL 제출 완료 평가 상세 조회 — 존재하지 않으면 예외")
    void getTlEvaluationDetail_notFound_throwsException() {
        // given
        given(mapper.findTlEvaluationDetail(99L, 200L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> service.getTlEvaluationDetail(200L, 99L))
                .isInstanceOf(BusinessException.class);
    }

    /* ── getDlEvaluationDetail (DL) ───────────────────────────────────── */

    @Test
    @DisplayName("DL 1차 평가 항목 조회 — 본인 부서 직원이면 정상 반환")
    void getDlEvaluationDetail_success() {
        // given
        com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationDetailResponse detail =
                new com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationDetailResponse();
        detail.setEvaluateeId(101L);
        given(mapper.findDlEvaluationDetail(200L, 101L, 5L)).willReturn(detail);

        // when
        com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.DlEvaluationDetailResponse result =
                service.getDlEvaluationDetail(200L, 101L, 5L);

        // then
        assertThat(result.getEvaluateeId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("DL 1차 평가 항목 조회 — 평가가 없으면 not found 예외")
    void getDlEvaluationDetail_notFound_throwsException() {
        // given
        given(mapper.findDlEvaluationDetail(200L, 999L, 5L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> service.getDlEvaluationDetail(200L, 999L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("평가를 찾을 수 없습니다.");
    }

    /* ── getEvaluationGradeSummary (HRM) ─────────────────────────────── */

    @Test
    @DisplayName("HRM 등급별 평가 집계 — 정상 반환")
    void getEvaluationGradeSummary_success() {
        // given
        EvaluationGradeSummaryItem item = new EvaluationGradeSummaryItem();
        item.setGrade("A");
        item.setCount(5L);
        given(mapper.findEvaluationGradeSummary(5L)).willReturn(List.of(item));

        // when
        List<EvaluationGradeSummaryItem> result = service.getEvaluationGradeSummary(5L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGrade()).isEqualTo("A");
    }
}
