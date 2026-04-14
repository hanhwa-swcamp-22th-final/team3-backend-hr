package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.appeal.AppealSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.AppealQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppealQueryService {

    private final AppealQueryMapper mapper;

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
        if (isTl && !mapper.existsReviewerAppealAccess(appealId, userDetails.getEmployeeId(), 1L)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (isDl && !mapper.existsReviewerAppealAccess(appealId, userDetails.getEmployeeId(), 2L)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return detail;
    }

    /* HRM — 이의신청 목록 조회 (상태 필터, 페이징) */
    public AppealListResponse getAppeals(String status, int page, int size) {
        int offset = page * size;
        List<AppealSummaryResponse> content = mapper.findAppeals(status, size, offset);
        long totalCount = mapper.countAppeals(status);
        long totalPages = (long) Math.ceil((double) totalCount / size);
        return new AppealListResponse(content, totalCount, totalPages);
    }

    /* Worker — 내 이의신청 목록 */
    public List<AppealSummaryResponse> getMyAppeals(Long employeeId) {
        return mapper.findMyAppeals(employeeId);
    }

    public List<AppealSummaryResponse> getTlAppeals(Long reviewerId) {
        return mapper.findReviewerAppeals(reviewerId, 1L, List.of("RECEIVING", "REVIEWING", "COMPLETED"));
    }

    public List<AppealSummaryResponse> getDlAppeals(Long reviewerId) {
        return mapper.findReviewerAppeals(reviewerId, 2L, List.of("REVIEWING", "COMPLETED"));
    }
}
