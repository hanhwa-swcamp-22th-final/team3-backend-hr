package com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.tierconfig.TierCriteriaItem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvaluationCriteriaResponse {

    private final List<TierCriteriaItem> tierConfigs;
    private final List<EvaluationCategoryWeightItem> categoryWeights;
    private final List<TierCriteriaHistoryItem> tierConfigHistory;
    private final List<EvaluationCategoryWeightHistoryItem> categoryWeightHistory;
    private final List<TierCriteriaHistoryGroupItem> tierConfigHistoryGroups;
    private final List<EvaluationCategoryWeightHistoryGroupItem> categoryWeightHistoryGroups;
}
