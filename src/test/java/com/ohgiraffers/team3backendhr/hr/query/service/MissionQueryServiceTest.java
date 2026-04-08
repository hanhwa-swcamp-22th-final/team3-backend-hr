package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.MissionResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.MissionQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MissionQueryServiceTest {

    @Mock
    private MissionQueryMapper missionQueryMapper;

    @InjectMocks
    private MissionQueryService missionQueryService;

    private MissionResponse sampleMission(Long id, String status) {
        return new MissionResponse(id, 10L, "미션명", "HIGH_DIFFICULTY_WORK", "B",
                BigDecimal.valueOf(5), BigDecimal.valueOf(10), 50, status, 200, null);
    }

    @Test
    @DisplayName("전체 미션 목록 — 페이지와 사이즈를 offset으로 변환해 mapper에 전달한다")
    void getMissions_delegatesToMapper() {
        // given
        given(missionQueryMapper.findAllByEmployeeId(99L, "IN_PROGRESS", 20, 10))
                .willReturn(List.of(sampleMission(1L, "IN_PROGRESS")));

        // when
        List<MissionResponse> result = missionQueryService.getMissions(99L, "IN_PROGRESS", 2, 10);

        // then
        assertThat(result).hasSize(1);
        verify(missionQueryMapper).findAllByEmployeeId(99L, "IN_PROGRESS", 20, 10);
    }

    @Test
    @DisplayName("전체 미션 목록 — 미션이 없으면 빈 목록 반환")
    void getMissions_empty_returnsEmptyList() {
        // given
        given(missionQueryMapper.findAllByEmployeeId(9999L, null, 0, 20)).willReturn(List.of());

        // when
        List<MissionResponse> result = missionQueryService.getMissions(9999L, null, 0, 20);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("티어 달성 미션 조회 — mapper 결과를 그대로 반환한다")
    void getUpgradeMissions_delegatesToMapper() {
        // given
        given(missionQueryMapper.findUpgradeByEmployeeId(99L))
                .willReturn(List.of(
                        sampleMission(1L, "IN_PROGRESS"),
                        sampleMission(2L, "COMPLETED")));

        // when
        List<MissionResponse> result = missionQueryService.getUpgradeMissions(99L);

        // then
        assertThat(result).hasSize(2);
        verify(missionQueryMapper).findUpgradeByEmployeeId(99L);
    }

    @Test
    @DisplayName("티어 달성 미션 — 미션이 없으면 빈 목록 반환")
    void getUpgradeMissions_empty_returnsEmptyList() {
        // given
        given(missionQueryMapper.findUpgradeByEmployeeId(9999L)).willReturn(List.of());

        // when
        List<MissionResponse> result = missionQueryService.getUpgradeMissions(9999L);

        // then
        assertThat(result).isEmpty();
    }
}
