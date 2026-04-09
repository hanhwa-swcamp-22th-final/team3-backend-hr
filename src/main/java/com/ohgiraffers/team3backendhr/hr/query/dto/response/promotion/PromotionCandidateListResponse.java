package com.ohgiraffers.team3backendhr.hr.query.dto.response.promotion;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PromotionCandidateListResponse {

    private final List<PromotionCandidateItem> items;
    private final long totalCount;
    private final long totalPages;
}
