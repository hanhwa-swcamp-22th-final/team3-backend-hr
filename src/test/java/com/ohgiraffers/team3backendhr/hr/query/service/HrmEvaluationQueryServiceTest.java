package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.AntiGamingFlagItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.BiasReportItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.tierconfig.TierCriteriaItem;
import com.ohgiraffers.team3backendhr.hr.query.mapper.HrmEvaluationQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class HrmEvaluationQueryServiceTest {

    @Mock
    private HrmEvaluationQueryMapper mapper;

    @InjectMocks
    private HrmEvaluationQueryService service;

    /* ── getBiasReport ────────────────────────────────────────────────── */

    @Test
    @DisplayName("편향 보정 이력 조회 — 정상 반환")
    void getBiasReport_returnsList() {
        // given
        BiasReportItem item = new BiasReportItem();
        item.setBiasCorrectionId(1L);
        given(mapper.findBiasReport()).willReturn(List.of(item));

        // when
        List<BiasReportItem> result = service.getBiasReport();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBiasCorrectionId()).isEqualTo(1L);
    }

    /* ── getAntiGamingFlags ───────────────────────────────────────────── */

    @Test
    @DisplayName("어뷰징 감지 목록 조회 — 정상 반환")
    void getAntiGamingFlags_returnsList() {
        // given
        AntiGamingFlagItem item = new AntiGamingFlagItem();
        item.setFlagId(1L);
        given(mapper.findAntiGamingFlags()).willReturn(List.of(item));

        // when
        List<AntiGamingFlagItem> result = service.getAntiGamingFlags();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFlagId()).isEqualTo(1L);
    }

    /* ── getCriteria ──────────────────────────────────────────────────── */

    @Test
    @DisplayName("평가 기준 조회 — 정상 반환")
    void getCriteria_returnsList() {
        // given
        given(mapper.findLatestCriteria()).willReturn(List.of());

        // when
        List<TierCriteriaItem> result = service.getCriteria();

        // then
        assertThat(result).isEmpty();
    }
}
