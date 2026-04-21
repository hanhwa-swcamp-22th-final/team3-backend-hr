package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.EvaluationCategoryWeightHistoryGroupItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.EvaluationCategoryWeightHistoryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.EvaluationCriteriaResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.TierCriteriaHistoryGroupItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.evaluationcriteria.TierCriteriaHistoryItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.AntiGamingFlagItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.qualitativeevaluation.BiasReportItem;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.tierconfig.TierCriteriaItem;
import com.ohgiraffers.team3backendhr.hr.query.mapper.HrmEvaluationQueryMapper;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HrmEvaluationQueryService {

    private static final List<String> TIER_ORDER = List.of("S", "A", "B", "C");
    private static final List<String> WEIGHT_KEY_ORDER = List.of(
        "SA:PRODUCTIVITY",
        "SA:EQUIPMENT_RESPONSE",
        "SA:PROCESS_INNOVATION",
        "SA:KNOWLEDGE_SHARING",
        "BC:PRODUCTIVITY",
        "BC:EQUIPMENT_RESPONSE",
        "BC:PROCESS_INNOVATION",
        "BC:KNOWLEDGE_SHARING"
    );

    private final HrmEvaluationQueryMapper mapper;
    private final AdminClient adminClient;

    /* 편향 보정 이력 조회 */
    public List<BiasReportItem> getBiasReport() {
        List<BiasReportItem> list = mapper.findBiasReport();
        if (!list.isEmpty()) {
            List<Long> ids = list.stream().map(BiasReportItem::getEvaluatorId).distinct().toList();
            Map<Long, EmployeeProfileResponse> profileMap = adminClient.getWorkerProfiles(ids).stream()
                    .collect(Collectors.toMap(EmployeeProfileResponse::getEmployeeId, p -> p));
            list.forEach(item -> {
                EmployeeProfileResponse p = profileMap.get(item.getEvaluatorId());
                if (p != null) item.setEvaluatorName(p.getEmployeeName());
            });
        }
        return list;
    }

    /* 어뷰징 감지 목록 조회 */
    public List<AntiGamingFlagItem> getAntiGamingFlags() {
        List<AntiGamingFlagItem> list = mapper.findAntiGamingFlags();
        if (!list.isEmpty()) {
            List<Long> ids = list.stream().map(AntiGamingFlagItem::getEmployeeId).distinct().toList();
            Map<Long, EmployeeProfileResponse> profileMap = adminClient.getWorkerProfiles(ids).stream()
                    .collect(Collectors.toMap(EmployeeProfileResponse::getEmployeeId, p -> p));
            list.forEach(item -> {
                EmployeeProfileResponse p = profileMap.get(item.getEmployeeId());
                if (p != null) item.setEmployeeName(p.getEmployeeName());
            });
        }
        return list;
    }

    public List<TierCriteriaItem> getTierCriteria() {
        return mapper.findLatestTierCriteria();
    }

    /* 평가 기준(가중치·임계값) 조회 — Grade별 최신 기준만 반환 */
    public EvaluationCriteriaResponse getEvaluationCriteriaDetail() {
        List<TierCriteriaHistoryItem> tierHistory = mapper.findTierCriteriaHistory();
        List<EvaluationCategoryWeightHistoryItem> weightHistory = mapper.findEvaluationCategoryWeightHistory();

        return new EvaluationCriteriaResponse(
            mapper.findLatestTierCriteria(),
            mapper.findLatestEvaluationCategoryWeights(),
            tierHistory,
            weightHistory,
            groupTierHistory(tierHistory),
            groupWeightHistory(weightHistory)
        );
    }

    private List<TierCriteriaHistoryGroupItem> groupTierHistory(List<TierCriteriaHistoryItem> historyItems) {
        Map<String, List<TierCriteriaHistoryItem>> groupedByTier = new LinkedHashMap<>();
        for (String tier : TIER_ORDER) {
            groupedByTier.put(tier, new ArrayList<>());
        }

        for (TierCriteriaHistoryItem item : historyItems) {
            groupedByTier.computeIfAbsent(item.getTier(), key -> new ArrayList<>()).add(item);
        }

        groupedByTier.values().forEach(items ->
            items.sort(Comparator.comparing(this::historyTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
        );

        int maxVersionCount = groupedByTier.values().stream()
            .mapToInt(List::size)
            .max()
            .orElse(0);

        List<TierCriteriaHistoryGroupItem> groups = new ArrayList<>();
        for (int index = 0; index < maxVersionCount; index++) {
            List<TierCriteriaHistoryItem> bundle = new ArrayList<>();
            for (String tier : TIER_ORDER) {
                List<TierCriteriaHistoryItem> items = groupedByTier.getOrDefault(tier, List.of());
                if (index < items.size()) {
                    bundle.add(items.get(index));
                }
            }

            if (!bundle.isEmpty()) {
                groups.add(new TierCriteriaHistoryGroupItem(
                    "tier-history-" + index,
                    bundle.stream().anyMatch(item -> Boolean.TRUE.equals(item.getActive())),
                    bundle.stream().allMatch(item -> Boolean.TRUE.equals(item.getDeleted())),
                    bundle.stream().map(TierCriteriaHistoryItem::getCreatedAt).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null),
                    bundle.stream().map(this::historyTimestamp).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null),
                    bundle
                ));
            }
        }
        return groups;
    }

    private List<EvaluationCategoryWeightHistoryGroupItem> groupWeightHistory(
        List<EvaluationCategoryWeightHistoryItem> historyItems
    ) {
        Map<String, List<EvaluationCategoryWeightHistoryItem>> groupedByCategory = new LinkedHashMap<>();
        for (String key : WEIGHT_KEY_ORDER) {
            groupedByCategory.put(key, new ArrayList<>());
        }

        for (EvaluationCategoryWeightHistoryItem item : historyItems) {
            groupedByCategory.computeIfAbsent(weightHistoryKey(item), key -> new ArrayList<>()).add(item);
        }

        groupedByCategory.values().forEach(items ->
            items.sort(Comparator.comparing(this::historyTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
        );

        int maxVersionCount = groupedByCategory.values().stream()
            .mapToInt(List::size)
            .max()
            .orElse(0);

        List<EvaluationCategoryWeightHistoryGroupItem> groups = new ArrayList<>();
        for (int index = 0; index < maxVersionCount; index++) {
            List<EvaluationCategoryWeightHistoryItem> bundle = new ArrayList<>();
            for (String key : WEIGHT_KEY_ORDER) {
                List<EvaluationCategoryWeightHistoryItem> items = groupedByCategory.getOrDefault(key, List.of());
                if (index < items.size()) {
                    bundle.add(items.get(index));
                }
            }

            if (!bundle.isEmpty()) {
                groups.add(new EvaluationCategoryWeightHistoryGroupItem(
                    "weight-history-" + index,
                    bundle.stream().anyMatch(item -> Boolean.TRUE.equals(item.getActive())),
                    bundle.stream().allMatch(item -> Boolean.TRUE.equals(item.getDeleted())),
                    bundle.stream().map(EvaluationCategoryWeightHistoryItem::getCreatedAt).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null),
                    bundle.stream().map(this::historyTimestamp).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null),
                    bundle
                ));
            }
        }
        return groups;
    }

    private LocalDateTime historyTimestamp(TierCriteriaHistoryItem item) {
        return item.getUpdatedAt() != null ? item.getUpdatedAt() : item.getCreatedAt();
    }

    private LocalDateTime historyTimestamp(EvaluationCategoryWeightHistoryItem item) {
        return item.getUpdatedAt() != null ? item.getUpdatedAt() : item.getCreatedAt();
    }

    private String weightHistoryKey(EvaluationCategoryWeightHistoryItem item) {
        return item.getTierGroup() + ":" + item.getCategoryCode();
    }
}
