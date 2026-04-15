package com.ohgiraffers.team3backendhr.hr.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.tierconfig.TierCriteriaItem;
import com.ohgiraffers.team3backendhr.hr.query.mapper.HrmEvaluationQueryMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TierCriteriaQueryServiceTest {

    @Mock
    private HrmEvaluationQueryMapper mapper;

    @InjectMocks
    private HrmEvaluationQueryService service;

    @Test
    @DisplayName("Tier 승급 기준 목록 조회 시 최신 기준을 반환한다")
    void getTierCriteriaReturnsList() {
        given(mapper.findLatestTierCriteria()).willReturn(List.of(
            new TierCriteriaItem(1L, "S", 100)
        ));

        List<TierCriteriaItem> result = service.getTierCriteria();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTier()).isEqualTo("S");
        assertThat(result.get(0).getTierConfigPromotionPoint()).isEqualTo(100);
    }
}
