package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.mission;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MissionSeedRequest {

    private List<Long> employeeIds;

    @Valid
    @NotEmpty
    private List<MissionTemplateSeedItem> templates;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MissionTemplateSeedItem {

        @NotNull
        private String missionName;

        @NotNull
        private String missionType;

        @NotNull
        private String upgradeToTier;

        @NotNull
        @Positive
        private BigDecimal conditionValue;

        @NotNull
        @Positive
        private Integer rewardPoint;

        private BigDecimal initialValue = BigDecimal.ZERO;
    }
}
