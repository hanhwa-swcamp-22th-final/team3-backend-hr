package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.KpiMemberDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.KpiMemberSummaryResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.ScmClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.ProductionStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiQueryService {

    private final AdminClient adminClient;
    private final ScmClient scmClient;

    /* HR-010: 팀원 정량 점수 산출 내역 조회 */
    public List<KpiMemberSummaryResponse> getTeamKpiSummary(Long leaderId, int year, int quarter) {
        return adminClient.getTeamMemberIds(leaderId).stream()
                .map(memberId -> {
                    ProductionStatsResponse stats =
                            scmClient.getProductionStats(memberId, year, quarter);
                    return KpiMemberSummaryResponse.builder()
                            .employeeId(memberId)
                            .productionScore(calcProductionScore(stats))
                            .eIdxScore(calcEIdxScore(stats))
                            .defectRateScore(calcDefectRateScore(stats))
                            .build();
                })
                .toList();
    }

    /* HR-011: 특정 팀원 정량 점수 산출 상세 조회 */
    public KpiMemberDetailResponse getMemberKpiDetail(Long employeeId, int year, int quarter) {
        ProductionStatsResponse stats = scmClient.getProductionStats(employeeId, year, quarter);
        return KpiMemberDetailResponse.builder()
                .employeeId(employeeId)
                .year(year)
                .quarter(quarter)
                .targetProduction(stats.getTargetProduction())
                .actualProduction(stats.getActualProduction())
                .defectRate(stats.getDefectRate())
                .eIdx(stats.getEIdx())
                .productionScore(calcProductionScore(stats))
                .eIdxScore(calcEIdxScore(stats))
                .defectRateScore(calcDefectRateScore(stats))
                .build();
    }

    /* 생산량 점수: (실제 / 기준) * 100, 최대 100점 */
    private BigDecimal calcProductionScore(ProductionStatsResponse stats) {
        if (stats.getTargetProduction().compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return stats.getActualProduction()
                .divide(stats.getTargetProduction(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .min(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /* E_idx 점수: eIdx * 100, 최대 100점 */
    private BigDecimal calcEIdxScore(ProductionStatsResponse stats) {
        return stats.getEIdx()
                .multiply(new BigDecimal("100"))
                .min(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /* 불량률 점수: (1 - defectRate/100) * 100, 최소 0점 */
    private BigDecimal calcDefectRateScore(ProductionStatsResponse stats) {
        return BigDecimal.ONE
                .subtract(stats.getDefectRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .multiply(new BigDecimal("100"))
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
