package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.KpiMemberDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.KpiMemberSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QuantitativeEvaluationQueryMapper;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiQueryService {

    private final AdminClient adminClient;
    private final QuantitativeEvaluationQueryMapper quantitativeEvaluationQueryMapper;

    /* HR-010: 팀원별 정량 점수 산출 내역 조회 */
    public List<KpiMemberSummaryResponse> getTeamKpiSummary(Long leaderId, int year, int evalSequence) {
        List<EmployeeProfileResponse> profiles = adminClient.getTeamMemberProfiles(leaderId);
        if (profiles.isEmpty()) return List.of();

        List<Long> memberIds = profiles.stream()
                .map(EmployeeProfileResponse::getEmployeeId)
                .distinct()
                .toList();
        Map<Long, KpiMemberSummaryResponse> currentScoreMap = quantitativeEvaluationQueryMapper
                .findTeamKpiSummary(memberIds, year, evalSequence)
                .stream()
                .collect(Collectors.toMap(
                        KpiMemberSummaryResponse::getEmployeeId,
                        Function.identity(),
                        (left, right) -> left
                ));
        Map<Long, KpiMemberSummaryResponse> latestScoreMap = quantitativeEvaluationQueryMapper
                .findLatestTeamKpiSummary(memberIds)
                .stream()
                .collect(Collectors.toMap(
                        KpiMemberSummaryResponse::getEmployeeId,
                        Function.identity(),
                        (left, right) -> left
                ));

        return profiles.stream()
                .map(profile -> mergeKpiSummary(
                        profile,
                        currentScoreMap.getOrDefault(profile.getEmployeeId(), latestScoreMap.get(profile.getEmployeeId()))
                ))
                .sorted(Comparator.comparing(KpiMemberSummaryResponse::getEmployeeName, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    /* HR-011: 특정 팀원 정량 점수 산출 상세 조회 */
    public List<KpiMemberDetailResponse> getMemberKpiDetail(Long employeeId, int year, int evalSequence) {
        EmployeeProfileResponse profile = adminClient.getWorkerProfile(employeeId);
        List<KpiMemberDetailResponse> details = quantitativeEvaluationQueryMapper.findMemberKpiDetail(employeeId, year, evalSequence);
        if (details.isEmpty()) {
            details = quantitativeEvaluationQueryMapper.findLatestMemberKpiDetail(employeeId);
        }
        details.forEach(detail -> detail.setEmployeeName(profile == null ? null : profile.getEmployeeName()));
        return details;
    }

    private KpiMemberSummaryResponse mergeKpiSummary(EmployeeProfileResponse profile, KpiMemberSummaryResponse score) {
        KpiMemberSummaryResponse response = score == null ? new KpiMemberSummaryResponse() : score;
        response.setEmployeeId(profile.getEmployeeId());
        response.setEmployeeName(profile.getEmployeeName());
        response.setEmployeeTier(profile.getCurrentTier() == null ? null : profile.getCurrentTier().name());
        if (response.getStatus() == null) {
            response.setStatus("NO_DATA");
        }
        return response;
    }
}
