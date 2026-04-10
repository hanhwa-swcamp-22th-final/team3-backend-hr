package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.TierCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.PromotionEventPublisher;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TierCriteriaCommandServiceTest {

    @Mock
    private TierConfigRepository tierConfigRepository;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private PromotionEventPublisher promotionEventPublisher;

    private TierCriteriaCommandService service;

    @BeforeEach
    void setUp() {
        service = new TierCriteriaCommandService(
            tierConfigRepository,
            idGenerator,
            promotionEventPublisher
        );
    }

    @Test
    @DisplayName("평가 기준 저장 - 개별 점수 필드로 저장 성공")
    void saveCriteria_success() {
        TierCriteriaSaveRequest req = new TierCriteriaSaveRequest(
            "S", 100, 90.0, 85.0, 80.0, 88.0, 82.0, 78.0
        );

        service.saveCriteria(List.of(req));

        verify(tierConfigRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("평가 기준 저장 - 여러 등급 동시 저장")
    void saveCriteria_multipleGrades() {
        TierCriteriaSaveRequest reqS = new TierCriteriaSaveRequest(
            "S", 100, 90.0, 85.0, 80.0, 88.0, 82.0, 78.0
        );
        TierCriteriaSaveRequest reqA = new TierCriteriaSaveRequest(
            "A", 80, 80.0, 75.0, 70.0, 78.0, 72.0, 68.0
        );

        service.saveCriteria(List.of(reqS, reqA));

        verify(tierConfigRepository, times(2)).save(any());
    }
}
