package com.ohgiraffers.team3backendhr.hr.command.application.dto.response.mission;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MissionSeedResponse {

    private final List<Long> missionTemplateIds;
    private final int createdProgressCount;
    private final int skippedProgressCount;
}
