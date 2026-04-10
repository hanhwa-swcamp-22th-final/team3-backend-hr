package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation.QuantitativeEvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation.QuantitativeEvaluationListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation.QuantitativeEvaluationSummaryItem;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QuantitativeEvaluationQueryMapper;
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
class QuantitativeEvaluationQueryServiceTest {

    @InjectMocks
    private QuantitativeEvaluationQueryService service;

    @Mock
    private QuantitativeEvaluationQueryMapper mapper;

    @Test
    @DisplayName("목록 조회 시 totalPages가 올바르게 계산된다")
    void getList_totalPages() {
        given(mapper.findList(null, null, 10, 0)).willReturn(List.of(new QuantitativeEvaluationSummaryItem()));
        given(mapper.countList(null, null)).willReturn(25L);

        QuantitativeEvaluationListResponse result = service.getList(null, null, 0, 10);

        assertThat(result.getTotalCount()).isEqualTo(25L);
        assertThat(result.getTotalPages()).isEqualTo(3L);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("목록이 비어 있으면 빈 리스트와 totalCount 0을 반환한다")
    void getList_empty() {
        given(mapper.findList(null, null, 10, 0)).willReturn(List.of());
        given(mapper.countList(null, null)).willReturn(0L);

        QuantitativeEvaluationListResponse result = service.getList(null, null, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalCount()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("상세 조회 성공 시 결과를 반환한다")
    void getDetail_success() {
        QuantitativeEvaluationDetailResponse response = new QuantitativeEvaluationDetailResponse();
        given(mapper.findById(1L)).willReturn(response);

        QuantitativeEvaluationDetailResponse result = service.getDetail(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 상세 조회 시 예외가 발생한다")
    void getDetail_fail_notFound() {
        given(mapper.findById(999L)).willReturn(null);

        assertThatThrownBy(() -> service.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.EVALUATION_NOT_FOUND.getMessage());
    }
}
