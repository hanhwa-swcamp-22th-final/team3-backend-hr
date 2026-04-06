package com.ohgiraffers.team3backendhr.appeal.query.service;

import com.ohgiraffers.team3backendhr.appeal.query.dto.response.AppealListResponse;
import com.ohgiraffers.team3backendhr.appeal.query.dto.response.AppealSummaryResponse;
import com.ohgiraffers.team3backendhr.appeal.query.dto.response.ScoreModificationLogResponse;
import com.ohgiraffers.team3backendhr.appeal.query.mapper.AppealQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppealQueryService {

    private final AppealQueryMapper mapper;

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

    /* HRM — 점수 수정 이력 */
    public List<ScoreModificationLogResponse> getScoreModificationLogs() {
        return mapper.findScoreModificationLogs();
    }
}
