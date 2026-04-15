package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.EvaluationCategoryWeightSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.EvaluationCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.criteria.TierCriteriaSaveRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcriteria.EvaluationCategory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcriteria.EvaluationTierGroup;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcriteria.EvaluationWeightConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationWeightConfigRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.TierConfigRepository;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.PromotionEventPublisher;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TierCriteriaCommandServiceTest {

    @Mock
    private TierConfigRepository tierConfigRepository;

    @Mock
    private EvaluationWeightConfigRepository evaluationWeightConfigRepository;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private PromotionEventPublisher promotionEventPublisher;

    private TierCriteriaCommandService service;

    @BeforeEach
    void setUp() {
        service = new TierCriteriaCommandService(
            tierConfigRepository,
            evaluationWeightConfigRepository,
            idGenerator,
            promotionEventPublisher
        );

        lenient().when(idGenerator.generate()).thenReturn(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L);
        lenient().when(tierConfigRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(evaluationWeightConfigRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("save criteria with promotion points and category weights")
    void saveCriteria_success() {
        EvaluationCriteriaSaveRequest request = new EvaluationCriteriaSaveRequest(
            List.of(new TierCriteriaSaveRequest("S", 100)),
            validCategoryWeights()
        );

        service.saveCriteria(request);

        verify(tierConfigRepository, times(1)).save(any());
        verify(evaluationWeightConfigRepository, times(8)).save(any());
        verify(promotionEventPublisher, times(1)).publishTierConfigSnapshot(any());
        verify(promotionEventPublisher, times(8)).publishEvaluationWeightConfigSnapshot(any());
    }

    @Test
    @DisplayName("save criteria for multiple tiers")
    void saveCriteria_multipleGrades() {
        EvaluationCriteriaSaveRequest request = new EvaluationCriteriaSaveRequest(
            List.of(
                new TierCriteriaSaveRequest("S", 100),
                new TierCriteriaSaveRequest("A", 80)
            ),
            validCategoryWeights()
        );

        service.saveCriteria(request);

        verify(tierConfigRepository, times(2)).save(any());
        verify(evaluationWeightConfigRepository, times(8)).save(any());
        verify(promotionEventPublisher, times(2)).publishTierConfigSnapshot(any());
        verify(promotionEventPublisher, times(8)).publishEvaluationWeightConfigSnapshot(any());
    }

    @Test
    @DisplayName("delete criteria soft deletes active tier configs")
    void deleteCriteria_success() {
        given(tierConfigRepository.findByTierConfigTierAndActiveTrueAndDeletedFalse(any()))
            .willReturn(Optional.of(TierConfig.builder()
                .tierConfigId(100L)
                .tierConfigTier(Grade.S)
                .tierConfigPromotionPoint(100)
                .active(Boolean.TRUE)
                .deleted(Boolean.FALSE)
                .build()));
        given(evaluationWeightConfigRepository
            .findByTierGroupAndCategoryCodeAndActiveTrueAndDeletedFalse(any(), any()))
            .willReturn(Optional.of(EvaluationWeightConfig.builder()
                .evaluationWeightConfigId(200L)
                .tierGroup(EvaluationTierGroup.SA)
                .categoryCode(EvaluationCategory.PRODUCTIVITY)
                .weightPercent(20)
                .active(Boolean.TRUE)
                .deleted(Boolean.FALSE)
                .build()));

        service.deleteCriteria();

        verify(tierConfigRepository, times(Grade.values().length)).save(any());
        verify(tierConfigRepository, never()).delete(any());
        verify(evaluationWeightConfigRepository,
            times(EvaluationTierGroup.values().length * EvaluationCategory.values().length)).save(any());
        verify(evaluationWeightConfigRepository, never()).delete(any());
        verify(promotionEventPublisher, times(Grade.values().length)).publishTierConfigSnapshot(any());
        verify(
            promotionEventPublisher,
            times(EvaluationTierGroup.values().length * EvaluationCategory.values().length)
        ).publishEvaluationWeightConfigSnapshot(any());
    }

    private List<EvaluationCategoryWeightSaveRequest> validCategoryWeights() {
        return List.of(
            new EvaluationCategoryWeightSaveRequest("SA", "PRODUCTIVITY", 20),
            new EvaluationCategoryWeightSaveRequest("SA", "EQUIPMENT_RESPONSE", 40),
            new EvaluationCategoryWeightSaveRequest("SA", "PROCESS_INNOVATION", 30),
            new EvaluationCategoryWeightSaveRequest("SA", "KNOWLEDGE_SHARING", 10),
            new EvaluationCategoryWeightSaveRequest("BC", "PRODUCTIVITY", 60),
            new EvaluationCategoryWeightSaveRequest("BC", "EQUIPMENT_RESPONSE", 20),
            new EvaluationCategoryWeightSaveRequest("BC", "PROCESS_INNOVATION", 10),
            new EvaluationCategoryWeightSaveRequest("BC", "KNOWLEDGE_SHARING", 10)
        );
    }
}
