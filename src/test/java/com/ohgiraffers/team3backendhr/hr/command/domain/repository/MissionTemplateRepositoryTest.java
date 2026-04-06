package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.MissionTemplate;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.MissionType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.UpgradeToTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MissionTemplateRepositoryTest {

    @Autowired
    private MissionTemplateRepository missionTemplateRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private MissionTemplate missionTemplate;
    private Long missionTemplateId;

    @BeforeEach
    void setUp() {
        missionTemplateId = idGenerator.generate();
        missionTemplate = MissionTemplate.builder()
                .missionTemplateId(missionTemplateId)
                .missionName("고난도 작업 미션")
                .missionType(MissionType.HIGH_DIFFICULTY_WORK)
                .upgradeToTier(UpgradeToTier.A)
                .conditionValue(BigDecimal.valueOf(5))
                .rewardPoint(100)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Save mission template success: mission template is persisted")
    void save_success() {
        MissionTemplate saved = missionTemplateRepository.save(missionTemplate);

        assertNotNull(saved);
        assertEquals(missionTemplateId, saved.getMissionTemplateId());
        assertEquals("고난도 작업 미션", saved.getMissionName());
        assertEquals(MissionType.HIGH_DIFFICULTY_WORK, saved.getMissionType());
        assertTrue(saved.getIsActive());
    }

    @Test
    @DisplayName("Find mission template by id success: return persisted mission template")
    void findById_success() {
        missionTemplateRepository.save(missionTemplate);

        Optional<MissionTemplate> result = missionTemplateRepository.findById(missionTemplateId);

        assertTrue(result.isPresent());
        assertEquals(UpgradeToTier.A, result.get().getUpgradeToTier());
    }

    @Test
    @DisplayName("Find mission template by id failure: return empty when mission template does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<MissionTemplate> result = missionTemplateRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find active mission templates: return only active templates")
    void findByIsActiveTrue_success() {
        Long inactiveId = idGenerator.generate();
        missionTemplateRepository.save(missionTemplate);
        missionTemplateRepository.save(MissionTemplate.builder()
                .missionTemplateId(inactiveId)
                .missionName("비활성 미션")
                .missionType(MissionType.KMS_CONTRIBUTION)
                .upgradeToTier(UpgradeToTier.B)
                .conditionValue(BigDecimal.valueOf(3))
                .rewardPoint(50)
                .isActive(false)
                .build());

        List<MissionTemplate> result = missionTemplateRepository.findByIsActiveTrue();

        assertTrue(result.stream().allMatch(MissionTemplate::getIsActive));
        assertTrue(result.stream().anyMatch(t -> t.getMissionTemplateId().equals(missionTemplateId)));
    }

    @Test
    @DisplayName("Find active mission templates by tier: return active templates matching upgrade tier")
    void findByUpgradeToTierAndIsActiveTrue_success() {
        missionTemplateRepository.save(missionTemplate);
        missionTemplateRepository.save(MissionTemplate.builder()
                .missionTemplateId(idGenerator.generate())
                .missionName("S 티어 미션")
                .missionType(MissionType.AI_SCORE)
                .upgradeToTier(UpgradeToTier.S)
                .conditionValue(BigDecimal.valueOf(90))
                .rewardPoint(200)
                .isActive(true)
                .build());

        List<MissionTemplate> result = missionTemplateRepository.findByUpgradeToTierAndIsActiveTrue(UpgradeToTier.A);

        assertTrue(result.stream().allMatch(t -> t.getUpgradeToTier() == UpgradeToTier.A));
        assertTrue(result.stream().allMatch(MissionTemplate::getIsActive));
    }
}
