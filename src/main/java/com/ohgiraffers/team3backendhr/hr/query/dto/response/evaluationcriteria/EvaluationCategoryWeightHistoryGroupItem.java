package com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvaluationCategoryWeightHistoryGroupItem {

    private final String versionKey;
    private final Boolean active;
    private final Boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<EvaluationCategoryWeightHistoryItem> items;
}
