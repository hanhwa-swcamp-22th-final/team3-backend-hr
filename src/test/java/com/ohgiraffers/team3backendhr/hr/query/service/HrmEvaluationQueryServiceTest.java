package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.EvaluationCategoryWeightItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.EvaluationCategoryWeightHistoryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.EvaluationCriteriaResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.TierCriteriaHistoryItem;
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

import java.time.LocalDateTime;
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
    void getEvaluationCriteriaDetail_returnsResponse() {
        // given
        given(mapper.findLatestTierCriteria()).willReturn(List.of(
            new TierCriteriaItem(1L, "S", 100)
        ));
        given(mapper.findLatestEvaluationCategoryWeights()).willReturn(List.of(
            new EvaluationCategoryWeightItem(10L, "SA", "PRODUCTIVITY", 20)
        ));
        given(mapper.findTierCriteriaHistory()).willReturn(List.of(
            new TierCriteriaHistoryItem(1L, "S", 100, true, false, LocalDateTime.now(), LocalDateTime.now())
        ));
        given(mapper.findEvaluationCategoryWeightHistory()).willReturn(List.of(
            new EvaluationCategoryWeightHistoryItem(10L, "SA", "PRODUCTIVITY", 20, true, false, LocalDateTime.now(), LocalDateTime.now())
        ));

        // when
        EvaluationCriteriaResponse result = service.getEvaluationCriteriaDetail();

        // then
        assertThat(result.getTierConfigs()).hasSize(1);
        assertThat(result.getCategoryWeights()).hasSize(1);
        assertThat(result.getTierConfigHistoryGroups()).hasSize(1);
        assertThat(result.getCategoryWeightHistoryGroups()).hasSize(1);
    }
}
