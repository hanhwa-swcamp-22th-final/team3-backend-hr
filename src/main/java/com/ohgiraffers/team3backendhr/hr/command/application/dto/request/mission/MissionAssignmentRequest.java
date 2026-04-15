package com.ohgiraffers.team3backendhr.hr.command.application.dto.request.mission;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MissionAssignmentRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private String currentTier;
}
