package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgEmployeeResponse {
    private Long employeeId;
    private String name;
    private String currentTier;
    private String departmentName;
    private String teamName;
    private String role;
}
