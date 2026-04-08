package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberAddRequest {
    @NotEmpty
    private List<Long> employeeIds;
}
