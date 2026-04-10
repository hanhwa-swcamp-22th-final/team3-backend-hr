package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentCreateRequest {
    private Long parentDepartmentId;
    @NotBlank
    private String departmentName;
    private String teamName;
    @NotBlank
    private String depth;
}
