package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MissionProgressTest {

    private MissionProgress buildProgress(MissionStatus status, BigDecimal currentValue) {
        return MissionProgress.builder()
                .missionProgressId(1L)
                .employeeId(100L)
                .missionTemplateId(1L)
                .currentValue(currentValue)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("목표 미달 시 currentValue가 갱신되고 IN_PROGRESS 상태가 유지된다")
    void updateProgress_목표미달_진행값갱신() {
        MissionProgress progress = buildProgress(MissionStatus.IN_PROGRESS, BigDecimal.valueOf(5));

        progress.updateProgress(BigDecimal.valueOf(7), BigDecimal.valueOf(10));

        assertThat(progress.getCurrentValue()).isEqualByComparingTo(BigDecimal.valueOf(7));
        assertThat(progress.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
        assertThat(progress.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("목표값 이상 달성 시 상태가 COMPLETED로 전이된다")
    void updateProgress_목표달성시_COMPLETED_전이() {
        MissionProgress progress = buildProgress(MissionStatus.IN_PROGRESS, BigDecimal.valueOf(7));

        progress.updateProgress(BigDecimal.valueOf(10), BigDecimal.valueOf(10));

        assertThat(progress.getStatus()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(progress.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("목표값 초과 달성 시에도 상태가 COMPLETED로 전이된다")
    void updateProgress_목표초과_COMPLETED_전이() {
        MissionProgress progress = buildProgress(MissionStatus.IN_PROGRESS, BigDecimal.valueOf(7));

        progress.updateProgress(BigDecimal.valueOf(12), BigDecimal.valueOf(10));

        assertThat(progress.getStatus()).isEqualTo(MissionStatus.COMPLETED);
    }

    @Test
    @DisplayName("이미 완료된 미션에 updateProgress 호출 시 예외가 발생한다")
    void updateProgress_이미완료된미션_예외() {
        MissionProgress progress = buildProgress(MissionStatus.COMPLETED, BigDecimal.valueOf(10));

        assertThatThrownBy(() ->
                progress.updateProgress(BigDecimal.valueOf(11), BigDecimal.valueOf(10))
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 완료된 미션입니다.");
    }
}
