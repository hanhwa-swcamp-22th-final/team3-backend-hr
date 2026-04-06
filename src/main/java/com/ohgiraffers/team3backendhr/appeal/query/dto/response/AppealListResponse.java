package com.ohgiraffers.team3backendhr.appeal.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AppealListResponse {

    private final List<AppealSummaryResponse> content;
    private final long totalCount;
    private final long totalPages;
}
