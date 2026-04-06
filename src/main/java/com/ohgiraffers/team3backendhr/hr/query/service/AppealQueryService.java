package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
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
                .orElseThrow(() -> new IllegalArgumentException("이의신청을 찾을 수 없습니다: " + appealId));

        boolean isWorker = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("WORKER"));
        if (isWorker && !detail.getAppealEmployeeId().equals(userDetails.getEmployeeId())) {
            throw new IllegalArgumentException("본인의 이의신청만 조회할 수 있습니다.");
        }
        return detail;
    }

    /* HRM — 이의신청 목록 조회 (상태 필터, 페이징) */
    public AppealListResponse getAppeals(String status, int page, int size) {
        int offset = (page - 1) * size;
        List<AppealSummaryResponse> content = mapper.findAppeals(status, size, offset);
        long totalCount = mapper.countAppeals(status);
        long totalPages = (long) Math.ceil((double) totalCount / size);
        return new AppealListResponse(content, totalCount, totalPages);
    }

    /* Worker — 내 이의신청 목록 */
    public List<AppealSummaryResponse> getMyAppeals(Long employeeId) {
        return mapper.findMyAppeals(employeeId);
    }


}
