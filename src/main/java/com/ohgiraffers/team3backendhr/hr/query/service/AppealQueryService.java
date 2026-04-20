package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.AppealQueryMapper;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.EmployeeProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppealQueryService {

    private final AppealQueryMapper mapper;
    private final AdminClient adminClient;

    /* HRM·Worker — 이의신청 상세 조회 (Worker는 본인 것만) */
    public AppealDetailResponse getAppeal(Long appealId, EmployeeUserDetails userDetails) {
        AppealDetailResponse detail = mapper.findAppealById(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPEAL_NOT_FOUND));

        boolean isWorker = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("WORKER"));
        boolean isTl = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TL"));
        boolean isDl = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("DL"));

        if (isWorker && !detail.getAppealEmployeeId().equals(userDetails.getEmployeeId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (isTl || isDl) {
            List<Long> memberIds = isTl
                    ? adminClient.getTeamMemberIds(userDetails.getEmployeeId())
                    : getDlMemberIds(userDetails.getEmployeeId());
            if (!memberIds.contains(detail.getAppealEmployeeId())) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        }

        enrichAppealDetail(detail);
        return detail;
    }

    /* HRM — 이의신청 목록 조회 (상태 필터, 페이징) */
    public AppealListResponse getAppeals(String status, Long evaluationPeriodId, int page, int size) {
        int offset = page * size;
        List<AppealSummaryResponse> content = mapper.findAppeals(status, evaluationPeriodId, size, offset);
        long totalCount = mapper.countAppeals(status, evaluationPeriodId);
        long totalPages = (long) Math.ceil((double) totalCount / size);
        enrichAppealList(content);
        return new AppealListResponse(content, totalCount, totalPages);
    }

    /* Worker — 내 이의신청 목록 */
    public List<AppealSummaryResponse> getMyAppeals(Long employeeId, Long evaluationPeriodId) {
        List<AppealSummaryResponse> list = mapper.findMyAppeals(employeeId, evaluationPeriodId);
        enrichAppealList(list);
        return list;
    }

    public List<AppealSummaryResponse> getTlAppeals(Long reviewerId, Long evaluationPeriodId) {
        List<Long> memberIds = adminClient.getTeamMemberIds(reviewerId);
        if (memberIds.isEmpty()) return List.of();
        List<AppealSummaryResponse> list = mapper.findReviewerAppeals(
                memberIds,
                evaluationPeriodId,
                List.of("RECEIVING", "REVIEWING", "COMPLETED"));
        enrichAppealList(list);
        return list;
    }

    public List<AppealSummaryResponse> getDlAppeals(Long reviewerId, Long evaluationPeriodId) {
        List<Long> memberIds = getDlMemberIds(reviewerId);
        if (memberIds.isEmpty()) return List.of();
        List<AppealSummaryResponse> list = mapper.findReviewerAppeals(
                memberIds,
                evaluationPeriodId,
                List.of("REVIEWING", "COMPLETED"));
        enrichAppealList(list);
        return list;
    }

    private void enrichAppealDetail(AppealDetailResponse detail) {
        EmployeeProfileResponse profile = adminClient.getWorkerProfile(detail.getAppealEmployeeId());
        if (profile != null) {
            detail.setEmployeeName(profile.getEmployeeName());
            detail.setEmployeeCode(profile.getEmployeeCode());
            detail.setEmployeeTier(profile.getCurrentTier() != null ? profile.getCurrentTier().name() : null);
            detail.setTeamName(profile.getTeamName());
            detail.setDepartmentName(profile.getDepartmentName());
        }
    }

    private void enrichAppealList(List<AppealSummaryResponse> list) {
        if (list.isEmpty()) return;
        List<Long> ids = list.stream()
                .map(AppealSummaryResponse::getAppealEmployeeId)
                .distinct()
                .toList();
        Map<Long, EmployeeProfileResponse> profileMap = adminClient.getWorkerProfiles(ids).stream()
                .collect(Collectors.toMap(EmployeeProfileResponse::getEmployeeId, p -> p));
        list.forEach(item -> {
            EmployeeProfileResponse p = profileMap.get(item.getAppealEmployeeId());
            if (p != null) {
                item.setEmployeeName(p.getEmployeeName());
                item.setEmployeeCode(p.getEmployeeCode());
                item.setEmployeeTier(p.getCurrentTier() != null ? p.getCurrentTier().name() : null);
                item.setTeamName(p.getTeamName());
                item.setDepartmentName(p.getDepartmentName());
            }
        });
    }

    private List<Long> getDlMemberIds(Long reviewerId) {
        EmployeeProfileResponse profile = adminClient.getWorkerProfile(reviewerId);
        if (profile == null || profile.getDepartmentId() == null) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return adminClient.getActiveWorkerIdsByRootDepartmentId(profile.getDepartmentId());
    }
}
