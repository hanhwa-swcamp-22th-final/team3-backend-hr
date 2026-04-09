package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionCandidateListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion.PromotionSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.PromotionQueryMapper;
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
class PromotionQueryServiceTest {

    @Mock
    private PromotionQueryMapper mapper;

    @InjectMocks
    private PromotionQueryService service;

    @Test
    @DisplayName("승급 요약 조회 — 확정 수·승급률 정상 계산")
    void getSummary_success() {
        given(mapper.countTotal()).willReturn(10L);
        given(mapper.countConfirmed()).willReturn(4L);

        PromotionSummaryResponse result = service.getSummary();

        assertThat(result.getTotalCandidates()).isEqualTo(10L);
        assertThat(result.getConfirmedCount()).isEqualTo(4L);
        assertThat(result.getPromotionRate()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("승급 요약 조회 — 대상 없으면 승급률 0")
    void getSummary_noCandidate_rateZero() {
        given(mapper.countTotal()).willReturn(0L);
        given(mapper.countConfirmed()).willReturn(0L);

        PromotionSummaryResponse result = service.getSummary();

        assertThat(result.getPromotionRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("후보 목록 조회 — 페이징 정상 반환")
    void getCandidates_success() {
        PromotionCandidateItem item = new PromotionCandidateItem();
        item.setTierPromotionId(1L);
        given(mapper.findCandidates(null, 10, 0)).willReturn(List.of(item));
        given(mapper.countCandidates(null)).willReturn(1L);

        PromotionCandidateListResponse result = service.getCandidates(null, 1, 10);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1L);
    }

    @Test
    @DisplayName("후보 상세 조회 — 정상 반환")
    void getCandidateDetail_success() {
        PromotionCandidateDetailResponse detail = new PromotionCandidateDetailResponse();
        detail.setTierPromotionId(1L);
        given(mapper.findCandidateById(1L)).willReturn(detail);

        PromotionCandidateDetailResponse result = service.getCandidateDetail(1L);

        assertThat(result.getTierPromotionId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("후보 상세 조회 — 존재하지 않으면 예외")
    void getCandidateDetail_notFound_throwsException() {
        given(mapper.findCandidateById(999L)).willReturn(null);

        assertThatThrownBy(() -> service.getCandidateDetail(999L))
                .isInstanceOf(BusinessException.class);
    }
}
