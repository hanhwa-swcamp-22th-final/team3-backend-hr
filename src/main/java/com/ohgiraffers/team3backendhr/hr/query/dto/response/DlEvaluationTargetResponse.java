package com.ohgiraffers.team3backendhr.hr.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DlEvaluationTargetResponse {

    private final Long evalPeriodId;
    private final List<DlEvaluationTargetItem> targets;
}
