package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationperiod.EvaluationPeriodDeadlineResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationperiod.EvaluationPeriodListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationperiod.EvaluationPeriodSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QualitativeEvaluationQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EvaluationPeriodQueryServiceTest {

    @Mock
    private QualitativeEvaluationQueryMapper mapper;

    @InjectMocks
    private EvaluationPeriodQueryService service;

    /* ── getEvaluationPeriods ─────────────────────────────────────────── */

    @Test
    @DisplayName("평가 기간 목록 조회 — content·totalElements·totalPages 반환")
    void getEvaluationPeriods_returnsPaginatedResult() {
        // given
        EvaluationPeriodSummaryResponse item = new EvaluationPeriodSummaryResponse();
        item.setEvalPeriodId(1L);
        given(mapper.findEvaluationPeriods(null, null, 10, 0)).willReturn(List.of(item));
        given(mapper.countEvaluationPeriods(null, null)).willReturn(1L);

        // when
        EvaluationPeriodListResponse result = service.getEvaluationPeriods(null, null, 0, 10);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1L);
    }

    @Test
    @DisplayName("평가 기간 목록 조회 — 결과 없으면 빈 목록 반환")
    void getEvaluationPeriods_emptyResult() {
        // given
        given(mapper.findEvaluationPeriods(2026, "IN_PROGRESS", 10, 0)).willReturn(List.of());
        given(mapper.countEvaluationPeriods(2026, "IN_PROGRESS")).willReturn(0L);

        // when
        EvaluationPeriodListResponse result = service.getEvaluationPeriods(2026, "IN_PROGRESS", 0, 10);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
        assertThat(result.getTotalPages()).isEqualTo(0L);
    }

    /* ── getDeadline ──────────────────────────────────────────────────── */

    @Test
    @DisplayName("마감일 조회 성공 — IN_PROGRESS 기간 있으면 반환")
    void getDeadline_success() {
        // given
        EvaluationPeriodDeadlineResponse response = new EvaluationPeriodDeadlineResponse();
        response.setEvalPeriodId(5L);
        response.setDaysRemaining(7L);
        given(mapper.findCurrentDeadline()).willReturn(response);

        // when
        EvaluationPeriodDeadlineResponse result = service.getDeadline();

        // then
        assertThat(result.getEvalPeriodId()).isEqualTo(5L);
        assertThat(result.getDaysRemaining()).isEqualTo(7L);
    }

    @Test
    @DisplayName("마감일 조회 — 진행 중인 기간 없으면 예외")
    void getDeadline_noInProgressPeriod_throwsException() {
        // given
        given(mapper.findCurrentDeadline()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> service.getDeadline())
                .isInstanceOf(BusinessException.class)
                .hasMessage("현재 진행 중인 평가 기간이 없습니다.");
    }
}
