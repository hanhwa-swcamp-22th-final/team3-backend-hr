package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamCreateRequest {
    @NotBlank
    private String teamName;
    @NotNull
    private Long leaderId;
}
