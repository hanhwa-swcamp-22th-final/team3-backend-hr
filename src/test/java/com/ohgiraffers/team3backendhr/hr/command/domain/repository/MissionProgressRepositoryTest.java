package com.ohgiraffers.team3backendhr.hr.command.domain.repository;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missionprogress.MissionProgress;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missionprogress.MissionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.MissionTemplate;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.MissionType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate.UpgradeToTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ohgiraffers.team3backendhr.config.TestAuditConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestAuditConfig.class)
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MissionProgressRepositoryTest {

    @Autowired
    private MissionProgressRepository missionProgressRepository;

    @Autowired
    private MissionTemplateRepository missionTemplateRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private MissionProgress missionProgress;
    private Long missionProgressId;
    private Long missionTemplateId;
    private final Long employeeId = 1001L;

    @BeforeEach
    void setUp() {
        missionTemplateId = idGenerator.generate();
        missionTemplateRepository.save(MissionTemplate.builder()
                .missionTemplateId(missionTemplateId)
                .missionName("고난도 작업 미션")
                .missionType(MissionType.HIGH_DIFFICULTY_WORK)
                .upgradeToTier(UpgradeToTier.A)
                .conditionValue(BigDecimal.valueOf(5))
                .rewardPoint(100)
                .isActive(true)
                .build());

        missionProgressId = idGenerator.generate();
        missionProgress = MissionProgress.builder()
                .missionProgressId(missionProgressId)
                .employeeId(employeeId)
                .missionTemplateId(missionTemplateId)
                .build();
    }

    @Test
    @DisplayName("Save mission progress success: mission progress is persisted")
    void save_success() {
        MissionProgress saved = missionProgressRepository.save(missionProgress);

        assertNotNull(saved);
        assertEquals(missionProgressId, saved.getMissionProgressId());
        assertEquals(MissionStatus.IN_PROGRESS, saved.getStatus());
        assertEquals(BigDecimal.ZERO, saved.getCurrentValue());
    }

    @Test
    @DisplayName("Find mission progress by id success: return persisted mission progress")
    void findById_success() {
        missionProgressRepository.save(missionProgress);

        Optional<MissionProgress> result = missionProgressRepository.findById(missionProgressId);

        assertTrue(result.isPresent());
        assertEquals(employeeId, result.get().getEmployeeId());
    }

    @Test
    @DisplayName("Find mission progress by id failure: return empty when mission progress does not exist")
    void findById_whenNotFound_thenEmpty() {
        Optional<MissionProgress> result = missionProgressRepository.findById(idGenerator.generate());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find mission progresses by employee id: return all progresses of the employee")
    void findByEmployeeId_success() {
        missionProgressRepository.save(missionProgress);

        List<MissionProgress> result = missionProgressRepository.findByEmployeeId(employeeId);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(p -> p.getEmployeeId().equals(employeeId)));
    }

    @Test
    @DisplayName("Find mission progresses by employee id and status: return progresses matching status")
    void findByEmployeeIdAndStatus_success() {
        missionProgressRepository.save(missionProgress);

        List<MissionProgress> result = missionProgressRepository
                .findByEmployeeIdAndStatus(employeeId, MissionStatus.IN_PROGRESS);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(p -> p.getStatus() == MissionStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("Find mission progress by employee id and template id: return matching progress")
    void findByEmployeeIdAndMissionTemplateId_success() {
        missionProgressRepository.save(missionProgress);

        Optional<MissionProgress> result = missionProgressRepository
                .findByEmployeeIdAndMissionTemplateId(employeeId, missionTemplateId);

        assertTrue(result.isPresent());
        assertEquals(missionProgressId, result.get().getMissionProgressId());
    }
}
