package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.ScoreModificationLogResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.AppealQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppealQueryServiceTest {

    @Mock
    private AppealQueryMapper mapper;

    @InjectMocks
    private AppealQueryService service;

    /* ── getAppeals ────────────────────────────────────────────────── */

    @Test
    @DisplayName("이의신청 목록 조회 — 상태 필터 없이 전체 반환")
    void getAppeals_noFilter() {
        // given
        AppealSummaryResponse item = new AppealSummaryResponse();
        item.setAppealId(1L);
        given(mapper.findAppeals(null, 10, 0)).willReturn(List.of(item));
        given(mapper.countAppeals(null)).willReturn(1L);

        // when
        var result = service.getAppeals(null, 1, 10);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이의신청 목록 조회 — 상태 필터 적용")
    void getAppeals_withStatusFilter() {
        // given
        AppealSummaryResponse item = new AppealSummaryResponse();
        item.setStatus("RECEIVING");
        given(mapper.findAppeals("RECEIVING", 10, 0)).willReturn(List.of(item));
        given(mapper.countAppeals("RECEIVING")).willReturn(1L);

        // when
        var result = service.getAppeals("RECEIVING", 1, 10);

        // then
        assertThat(result.getContent()).allMatch(r -> "RECEIVING".equals(r.getStatus()));
        verify(mapper).findAppeals("RECEIVING", 10, 0);
    }

    /* ── getMyAppeals ──────────────────────────────────────────────── */

    @Test
    @DisplayName("내 이의신청 조회 — 본인 목록 반환")
    void getMyAppeals_success() {
        // given
        AppealSummaryResponse item = new AppealSummaryResponse();
        item.setAppealEmployeeId(100L);
        given(mapper.findMyAppeals(100L)).willReturn(List.of(item));

        // when
        List<AppealSummaryResponse> result = service.getMyAppeals(100L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppealEmployeeId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("내 이의신청 조회 — 데이터 없으면 빈 목록")
    void getMyAppeals_empty() {
        // given
        given(mapper.findMyAppeals(999L)).willReturn(List.of());

        // when
        List<AppealSummaryResponse> result = service.getMyAppeals(999L);

        // then
        assertThat(result).isEmpty();
    }

    /* ── getScoreModificationLogs ──────────────────────────────────── */

    @Test
    @DisplayName("점수 수정 이력 조회 — 전체 목록 반환")
    void getScoreModificationLogs_success() {
        // given
        ScoreModificationLogResponse log = new ScoreModificationLogResponse();
        log.setScoreModificationLogId(1L);
        given(mapper.findScoreModificationLogs()).willReturn(List.of(log));

        // when
        List<ScoreModificationLogResponse> result = service.getScoreModificationLogs();

        // then
        assertThat(result).hasSize(1);
        verify(mapper).findScoreModificationLogs();
    }
}
