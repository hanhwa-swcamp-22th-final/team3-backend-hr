package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.common.idgenerator.TimeBasedIdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.MissionProgress;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.MissionStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.MissionTemplate;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.MissionType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.UpgradeToTier;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.MissionProgressRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.MissionTemplateRepository;
import com.ohgiraffers.team3backendhr.hr.query.dto.MissionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MissionQueryMapperTest {

    @Autowired
    private MissionQueryMapper mapper;

    @Autowired
    private MissionTemplateRepository missionTemplateRepository;

    @Autowired
    private MissionProgressRepository missionProgressRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();
    private final Long employeeId = 500L;

    private Long templateAId;
    private Long templateBId;
    private Long progressInProgressId;
    private Long progressCompletedId;

    @BeforeEach
    void setUp() {
        // 미션 쿼리가 employee 테이블 JOIN + 티어 필터를 사용하므로 테스트용 사원 삽입.
        // C티어 사원 → upgrade_to_tier='B' 인 IN_PROGRESS 미션이 조회되어야 함
        jdbcTemplate.update(
                "INSERT IGNORE INTO employee(employee_id, employee_code, employee_password, employee_role, employee_tier) " +
                "VALUES (?, 'TEST-500', 'pw', 'WORKER', 'C')",
                employeeId);

        // 템플릿 A — B티어 달성 목표, conditionValue=10
        templateAId = idGenerator.generate();
        missionTemplateRepository.saveAndFlush(MissionTemplate.builder()
                .missionTemplateId(templateAId)
                .missionName("고난도 작업 미션")
                .missionType(MissionType.HIGH_DIFFICULTY_WORK)
                .upgradeToTier(UpgradeToTier.B)
                .conditionValue(BigDecimal.valueOf(10))
                .rewardPoint(200)
                .isActive(true)
                .build());

        // 템플릿 B — A티어 달성 목표, conditionValue=5
        templateBId = idGenerator.generate();
        missionTemplateRepository.saveAndFlush(MissionTemplate.builder()
                .missionTemplateId(templateBId)
                .missionName("KMS 기여 미션")
                .missionType(MissionType.KMS_CONTRIBUTION)
                .upgradeToTier(UpgradeToTier.A)
                .conditionValue(BigDecimal.valueOf(5))
                .rewardPoint(300)
                .isActive(true)
                .build());

        // 진행 중 미션 — currentValue=5/conditionValue=10 → progressRate=50
        progressInProgressId = idGenerator.generate();
        missionProgressRepository.saveAndFlush(MissionProgress.builder()
                .missionProgressId(progressInProgressId)
                .employeeId(employeeId)
                .missionTemplateId(templateAId)
                .currentValue(BigDecimal.valueOf(5))
                .status(MissionStatus.IN_PROGRESS)
                .build());

        // 완료 미션 — currentValue=5/conditionValue=5 → progressRate=100
        progressCompletedId = idGenerator.generate();
        missionProgressRepository.saveAndFlush(MissionProgress.builder()
                .missionProgressId(progressCompletedId)
                .employeeId(employeeId)
                .missionTemplateId(templateBId)
                .currentValue(BigDecimal.valueOf(5))
                .status(MissionStatus.COMPLETED)
                .completedAt(LocalDateTime.of(2026, 3, 1, 12, 0))
                .build());
    }

    @Test
    @DisplayName("전체 미션 목록 조회 — 필터 없이 해당 직원의 미션 모두 반환")
    void findAllByEmployeeId_noFilter_returnsAll() {
        List<MissionResponse> result = mapper.findAllByEmployeeId(employeeId, null, 0, 20);

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(MissionResponse::getMissionProgressId).toList())
                .containsExactlyInAnyOrder(progressInProgressId, progressCompletedId);
    }

    @Test
    @DisplayName("전체 미션 목록 조회 — IN_PROGRESS 필터 적용")
    void findAllByEmployeeId_statusFilter_inProgress() {
        List<MissionResponse> result = mapper.findAllByEmployeeId(employeeId, "IN_PROGRESS", 0, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMissionProgressId()).isEqualTo(progressInProgressId);
        assertThat(result.get(0).getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("전체 미션 목록 조회 — COMPLETED 필터 적용")
    void findAllByEmployeeId_statusFilter_completed() {
        List<MissionResponse> result = mapper.findAllByEmployeeId(employeeId, "COMPLETED", 0, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMissionProgressId()).isEqualTo(progressCompletedId);
    }

    @Test
    @DisplayName("전체 미션 목록 조회 — 진행률이 올바르게 계산된다")
    void findAllByEmployeeId_progressRateCalculated() {
        List<MissionResponse> result = mapper.findAllByEmployeeId(employeeId, "IN_PROGRESS", 0, 20);

        assertThat(result.get(0).getProgressRate()).isEqualTo(50); // 5/10 * 100 = 50
    }

    @Test
    @DisplayName("전체 미션 목록 조회 — 완료 미션의 진행률은 100이다")
    void findAllByEmployeeId_completedProgressRate100() {
        List<MissionResponse> result = mapper.findAllByEmployeeId(employeeId, "COMPLETED", 0, 20);

        assertThat(result.get(0).getProgressRate()).isEqualTo(100); // 5/5 * 100 = 100
    }

    @Test
    @DisplayName("전체 미션 목록 조회 — 다른 직원의 미션은 포함되지 않는다")
    void findAllByEmployeeId_excludesOtherEmployees() {
        List<MissionResponse> result = mapper.findAllByEmployeeId(9999L, null, 0, 20);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("티어 달성 미션 조회 — is_active=true 인 미션만 반환한다")
    void findUpgradeByEmployeeId_returnsActiveMissions() {
        List<MissionResponse> result = mapper.findUpgradeByEmployeeId(employeeId);

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(MissionResponse::getMissionProgressId).toList())
                .containsExactlyInAnyOrder(progressInProgressId, progressCompletedId);
    }

    @Test
    @DisplayName("티어 달성 미션 조회 — upgradeToTier 오름차순으로 정렬된다 (B → A → S)")
    void findUpgradeByEmployeeId_sortedByTier() {
        List<MissionResponse> result = mapper.findUpgradeByEmployeeId(employeeId);

        assertThat(result.get(0).getUpgradeToTier()).isEqualTo("B");
        assertThat(result.get(1).getUpgradeToTier()).isEqualTo("A");
    }
}
