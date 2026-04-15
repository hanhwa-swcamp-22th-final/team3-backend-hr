package com.ohgiraffers.team3backendhr.hr.command.application.dto.response.mission;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MissionAssignmentResponse {

    private Long employeeId;
    private String upgradeToTier;
    private int createdProgressCount;
    private int skippedProgressCount;
}
