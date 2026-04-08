package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDetailResponse {
    private Long departmentId;
    private String departmentName;
    private int teamCount;
    private int totalMembers;
    private List<TeamSummaryResponse> teams;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamSummaryResponse {
        private Long teamId;
        private String teamName;
        private int memberCount;
    }
}
