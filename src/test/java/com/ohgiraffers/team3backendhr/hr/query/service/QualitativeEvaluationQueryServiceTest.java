package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.DlEvaluationTargetItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.TlEvaluationTargetItem;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QualitativeEvaluationQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QualitativeEvaluationQueryServiceTest {

    @Mock
    private QualitativeEvaluationQueryMapper mapper;

    @InjectMocks
    private QualitativeEvaluationQueryService service;

    /* ── getTlTargets ─────────────────────────────────────────────────── */

    @Test
    @DisplayName("TL 평가 대상 조회 — periodId 전달 시 그대로 사용")
    void getTlTargets_withPeriodId_returnsList() {
        // given
        TlEvaluationTargetItem item = new TlEvaluationTargetItem();
        item.setEvaluateeId(101L);
        given(mapper.findTlTargets(200L, 5L)).willReturn(List.of(item));

        // when
        Map<String, Object> result = service.getTlTargets(200L, 5L);

        // then
        assertThat(result.get("evalPeriodId")).isEqualTo(5L);
        assertThat((List<?>) result.get("targets")).hasSize(1);
    }

    @Test
    @DisplayName("TL 평가 대상 조회 — periodId null 이면 현재 기간으로 자동 resolve")
    void getTlTargets_withNullPeriodId_resolvesToCurrent() {
        // given
        given(mapper.findCurrentPeriodId()).willReturn(10L);
        given(mapper.findTlTargets(200L, 10L)).willReturn(List.of());

        // when
        Map<String, Object> result = service.getTlTargets(200L, null);

        // then
        assertThat(result.get("evalPeriodId")).isEqualTo(10L);
    }

    @Test
    @DisplayName("TL 평가 대상 조회 — periodId null이고 현재 기간 없으면 예외")
    void getTlTargets_noCurrentPeriod_throwsException() {
        // given
        given(mapper.findCurrentPeriodId()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> service.getTlTargets(200L, null))
                .isInstanceOf(IllegalStateException.class)
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

        // when
        Map<String, Object> result = service.getDlTargets(300L, 5L);

        // then
        assertThat(result.get("evalPeriodId")).isEqualTo(5L);
        assertThat((List<?>) result.get("targets")).hasSize(1);
    }

    @Test
    @DisplayName("DL 평가 대상 조회 — periodId null이면 현재 기간으로 자동 resolve")
    void getDlTargets_withNullPeriodId_resolvesToCurrent() {
        // given
        given(mapper.findCurrentPeriodId()).willReturn(10L);
        given(mapper.findDlTargets(300L, 10L)).willReturn(List.of());

        // when
        Map<String, Object> result = service.getDlTargets(300L, null);

        // then
        assertThat(result.get("evalPeriodId")).isEqualTo(10L);
    }
}
