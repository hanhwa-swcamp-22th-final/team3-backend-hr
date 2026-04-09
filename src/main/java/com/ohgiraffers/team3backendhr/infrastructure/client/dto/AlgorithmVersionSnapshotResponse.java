package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlgorithmVersionSnapshotResponse {

    private Long algorithmVersionId;
    private String versionNo;
    private String implementationKey;
    private String description;
    private Boolean isActive;
    private String parameters;
    private String referenceValues;
}
