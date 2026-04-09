package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.KpiMemberDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.KpiMemberSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QuantitativeEvaluationQueryMapper;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiQueryService {

    private final AdminClient adminClient;
    private final QuantitativeEvaluationQueryMapper quantitativeEvaluationQueryMapper;

    /* HR-010: 팀원별 정량 점수 산출 내역 조회 */
    public List<KpiMemberSummaryResponse> getTeamKpiSummary(Long leaderId, int year, int quarter) {
        List<Long> memberIds = adminClient.getTeamMemberIds(leaderId);
        if (memberIds.isEmpty()) return List.of();
        return quantitativeEvaluationQueryMapper.findTeamKpiSummary(memberIds, year, quarter);
    }

    /* HR-011: 특정 팀원 정량 점수 산출 상세 조회 */
    public List<KpiMemberDetailResponse> getMemberKpiDetail(Long employeeId, int year, int quarter) {
        return quantitativeEvaluationQueryMapper.findMemberKpiDetail(employeeId, year, quarter);
    }
}
