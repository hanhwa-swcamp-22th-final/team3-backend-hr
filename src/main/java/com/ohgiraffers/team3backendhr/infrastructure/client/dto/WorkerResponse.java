package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerResponse {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
}
