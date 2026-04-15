package com.ohgiraffers.team3backendhr.hr.query.mapper;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.EvaluationCategoryWeightItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.EvaluationCategoryWeightHistoryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.TierCriteriaHistoryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.AntiGamingFlagItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.BiasReportItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.tierconfig.TierCriteriaItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmEvaluationQueryMapper {

    /* 편향 보정 이력 전체 조회 */
    List<BiasReportItem> findBiasReport();

    /* 어뷰징 감지 목록 조회 */
    List<AntiGamingFlagItem> findAntiGamingFlags();

    /* Grade별 최신 평가 기준 조회 */
    List<TierCriteriaItem> findLatestTierCriteria();

    List<EvaluationCategoryWeightItem> findLatestEvaluationCategoryWeights();

    List<TierCriteriaHistoryItem> findTierCriteriaHistory();

    List<EvaluationCategoryWeightHistoryItem> findEvaluationCategoryWeightHistory();
}
