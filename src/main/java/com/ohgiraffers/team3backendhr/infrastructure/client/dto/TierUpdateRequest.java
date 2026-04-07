package com.ohgiraffers.team3backendhr.infrastructure.client.dto;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TierUpdateRequest {

    private Grade tier;
}
