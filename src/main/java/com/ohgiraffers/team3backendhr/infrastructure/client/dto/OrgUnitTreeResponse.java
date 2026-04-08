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
public class OrgUnitTreeResponse {
    private Long unitId;
    private String unitName;
    private String type;           // DEPARTMENT / TEAM
    private List<OrgUnitTreeResponse> children;
}
