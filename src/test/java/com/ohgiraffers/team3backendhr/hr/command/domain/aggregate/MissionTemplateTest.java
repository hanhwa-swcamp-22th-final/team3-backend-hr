package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MissionTemplateTest {

    private MissionTemplate buildTemplate(BigDecimal conditionValue) {
        return MissionTemplate.builder()
                .missionTemplateId(1L)
                .missionName("고난도 연속 10회 완료")
                .missionType(MissionType.HIGH_DIFFICULTY_WORK)
                .upgradeToTier(UpgradeToTier.S)
                .conditionValue(conditionValue)
                .rewardPoint(500)
                .build();
    }

    @Test
    @DisplayName("currentValue가 conditionValue 미만이면 isAchieved는 false를 반환한다")
    void isAchieved_목표미달() {
        MissionTemplate template = buildTemplate(BigDecimal.valueOf(10));

        assertThat(template.isAchieved(BigDecimal.valueOf(7))).isFalse();
    }

    @Test
    @DisplayName("currentValue가 conditionValue와 같으면 isAchieved는 true를 반환한다")
    void isAchieved_목표정확달성() {
        MissionTemplate template = buildTemplate(BigDecimal.valueOf(10));

        assertThat(template.isAchieved(BigDecimal.valueOf(10))).isTrue();
    }

    @Test
    @DisplayName("currentValue가 conditionValue를 초과해도 isAchieved는 true를 반환한다")
    void isAchieved_목표초과() {
        MissionTemplate template = buildTemplate(BigDecimal.valueOf(10));

        assertThat(template.isAchieved(BigDecimal.valueOf(12))).isTrue();
    }

    @Test
    @DisplayName("진행 중일 때 진행률은 올바르게 계산된다")
    void calculateProgressRate_진행중() {
        MissionTemplate template = buildTemplate(BigDecimal.valueOf(10));

        assertThat(template.calculateProgressRate(BigDecimal.valueOf(7))).isEqualTo(70);
    }

    @Test
    @DisplayName("currentValue가 conditionValue를 초과해도 진행률은 100을 넘지 않는다")
    void calculateProgressRate_초과시_100_상한() {
        MissionTemplate template = buildTemplate(BigDecimal.valueOf(10));

        assertThat(template.calculateProgressRate(BigDecimal.valueOf(12))).isEqualTo(100);
    }

    @Test
    @DisplayName("currentValue가 0이면 진행률은 0이다")
    void calculateProgressRate_시작전() {
        MissionTemplate template = buildTemplate(BigDecimal.valueOf(10));

        assertThat(template.calculateProgressRate(BigDecimal.ZERO)).isEqualTo(0);
    }
}
