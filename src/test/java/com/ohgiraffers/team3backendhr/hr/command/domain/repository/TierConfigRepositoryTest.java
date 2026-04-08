package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.TierConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TierConfigRepositoryTest {

    @Autowired
    private TierConfigRepository tierConfigRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private TierConfig tierConfig;
    private Long tierConfigId;

    @BeforeEach
    void setUp() {
        tierConfigId = idGenerator.generate();
        tierConfig = TierConfig.builder()
                .tierConfigId(tierConfigId)
                .tierConfigTier(Grade.B)
                .tierConfigWeightDistribution("{\"qualitative\":0.6,\"quantitative\":0.4}")
                .tierConfigPromotionPoint(300)
                .build();
    }

    @Test
    @DisplayName("Save tier config success: tier config is persisted")
    void save_success() {
        TierConfig saved = tierConfigRepository.save(tierConfig);

        assertNotNull(saved);
        assertEquals(tierConfigId, saved.getTierConfigId());
        assertEquals(Grade.B, saved.getTierConfigTier());
    }

    @Test
    @DisplayName("Find tier config by id success: return persisted tier config")
    void findById_success() {
        tierConfigRepository.save(tierConfig);

        Optional<TierConfig> result = tierConfigRepository.findById(tierConfigId);

        assertTrue(result.isPresent());
        assertEquals(Grade.B, result.get().getTierConfigTier());
    }

    @Test
    @DisplayName("Find tier config by id failure: return empty when tier config does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<TierConfig> result = tierConfigRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find tier config by tier: return tier config matching grade")
    void findByTierConfigTier_success() {
        tierConfigRepository.save(tierConfig);

        Optional<TierConfig> result = tierConfigRepository.findByTierConfigTier(Grade.B);

        assertTrue(result.isPresent());
        assertEquals(tierConfigId, result.get().getTierConfigId());
    }

    @Test
    @DisplayName("Find tier config by tier failure: return empty when grade does not exist")
    void findByTierConfigTier_whenNotFound_thenEmpty() {
        Optional<TierConfig> result = tierConfigRepository.findByTierConfigTier(Grade.S);

        assertFalse(result.isPresent());
    }
}
