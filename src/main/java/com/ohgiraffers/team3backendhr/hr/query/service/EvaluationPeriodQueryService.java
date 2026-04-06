package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.response.EvaluationPeriodDeadlineResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.EvaluationPeriodListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.EvaluationPeriodSummaryResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QualitativeEvaluationQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluationPeriodQueryService {

    private final QualitativeEvaluationQueryMapper mapper;

    /* 평가 기간 목록 조회 — year·status 필터 + 페이징 (HRM 전용) */
    public EvaluationPeriodListResponse getEvaluationPeriods(Integer year, String status, int page, int size) {
        int offset = page * size;
        List<EvaluationPeriodSummaryResponse> content = mapper.findEvaluationPeriods(year, status, size, offset);
        long totalElements = mapper.countEvaluationPeriods(year, status);
        long totalPages = (totalElements + size - 1) / size;

        return new EvaluationPeriodListResponse(content, totalElements, totalPages);
    }

    /* 마감일 조회 — 현재 IN_PROGRESS 기간의 endDate·daysRemaining 반환 (TL·DL 전용) */
    public EvaluationPeriodDeadlineResponse getDeadline() {
        EvaluationPeriodDeadlineResponse response = mapper.findCurrentDeadline();
        if (response == null) {
            // IN_PROGRESS 기간이 없으면 TL·DL이 평가할 수 없는 상태
            throw new IllegalStateException("현재 진행 중인 평가 기간이 없습니다.");
        }
        return response;
    }
}
