package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionHistory;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.promotionhistory.PromotionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ohgiraffers.team3backendhr.config.TestAuditConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestAuditConfig.class)
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PromotionHistoryRepositoryTest {

    @Autowired
    private PromotionHistoryRepository promotionHistoryRepository;

    @Autowired
    private TierConfigRepository tierConfigRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private PromotionHistory promotionHistory;
    private Long promotionHistoryId;
    private Long currentTierConfigId;
    private Long targetTierConfigId;
    private final Long employeeId = 1001L;
    private final Long reviewerId = 2001L;

    @BeforeEach
    void setUp() {
        currentTierConfigId = idGenerator.generate();
        targetTierConfigId = idGenerator.generate();

        tierConfigRepository.save(TierConfig.builder()
                .tierConfigId(currentTierConfigId)
                .tierConfigTier(Grade.B)
                .tierConfigWeightDistribution("{\"qualitative\":0.6,\"quantitative\":0.4}")
                .tierConfigPromotionPoint(300)
                .build());

        tierConfigRepository.save(TierConfig.builder()
                .tierConfigId(targetTierConfigId)
                .tierConfigTier(Grade.A)
                .tierConfigWeightDistribution("{\"qualitative\":0.7,\"quantitative\":0.3}")
                .tierConfigPromotionPoint(500)
                .build());

        promotionHistoryId = idGenerator.generate();
        promotionHistory = PromotionHistory.builder()
                .tierPromotionId(promotionHistoryId)
                .employeeId(employeeId)
                .reviewerId(reviewerId)
                .currentTierConfigId(currentTierConfigId)
                .targetTierConfigId(targetTierConfigId)
                .tierAccumulatedPoint(320)
                .build();
    }

    @Test
    @DisplayName("Save promotion history success: promotion history is persisted")
    void save_success() {
        PromotionHistory saved = promotionHistoryRepository.save(promotionHistory);

        assertNotNull(saved);
        assertEquals(promotionHistoryId, saved.getTierPromotionId());
        assertEquals(PromotionStatus.UNDER_REVIEW, saved.getTierPromoStatus());
    }

    @Test
    @DisplayName("Find promotion history by id success: return persisted promotion history")
    void findById_success() {
        promotionHistoryRepository.save(promotionHistory);

        Optional<PromotionHistory> result = promotionHistoryRepository.findById(promotionHistoryId);

        assertTrue(result.isPresent());
        assertEquals(employeeId, result.get().getEmployeeId());
    }

    @Test
    @DisplayName("Find promotion history by id failure: return empty when promotion history does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<PromotionHistory> result = promotionHistoryRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find promotion histories by employee id: return all histories of the employee")
    void findByEmployeeId_success() {
        promotionHistoryRepository.save(promotionHistory);

        List<PromotionHistory> result = promotionHistoryRepository.findByEmployeeId(employeeId);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(h -> h.getEmployeeId().equals(employeeId)));
    }

    @Test
    @DisplayName("Find promotion histories by status: return histories matching promo status")
    void findByTierPromoStatus_success() {
        promotionHistoryRepository.save(promotionHistory);

        List<PromotionHistory> result = promotionHistoryRepository.findByTierPromoStatus(PromotionStatus.UNDER_REVIEW);

        assertTrue(result.stream().allMatch(h -> h.getTierPromoStatus() == PromotionStatus.UNDER_REVIEW));
    }
}
