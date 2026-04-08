package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.TierCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TierCriteriaCommandServiceTest {

    @Mock
    private TierConfigRepository tierConfigRepository;

    @Mock
    private IdGenerator idGenerator;

    private TierCriteriaCommandService service;

    @BeforeEach
    void setUp() {
        service = new TierCriteriaCommandService(tierConfigRepository, idGenerator, new ObjectMapper());
    }

    @Test
    @DisplayName("평가 기준 저장 — 가중치 합계 100%이면 저장 성공")
    void saveCriteria_success() {
        // given — 합계 100%
        TierCriteriaSaveRequest req = new TierCriteriaSaveRequest(
                "S", "{\"성과\":60,\"역량\":40}", 100);

        // when
        service.saveCriteria(List.of(req));

        // then
        verify(tierConfigRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("평가 기준 저장 — 가중치 합계 100% 아니면 예외")
    void saveCriteria_invalidWeightSum_throwsException() {
        // given — 합계 90%
        TierCriteriaSaveRequest req = new TierCriteriaSaveRequest(
                "S", "{\"성과\":50,\"역량\":40}", 100);

        // when & then
        assertThatThrownBy(() -> service.saveCriteria(List.of(req)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("100");
    }
}
