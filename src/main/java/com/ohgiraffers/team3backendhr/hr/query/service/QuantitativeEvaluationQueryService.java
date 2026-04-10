package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation.QuantitativeEvaluationDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation.QuantitativeEvaluationListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.quantitativeevaluation.QuantitativeEvaluationSummaryItem;
import com.ohgiraffers.team3backendhr.hr.query.mapper.QuantitativeEvaluationQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuantitativeEvaluationQueryService {

    private final QuantitativeEvaluationQueryMapper mapper;

    public QuantitativeEvaluationListResponse getList(Long periodId, String status, int page, int size) {
        int offset = page * size;
        List<QuantitativeEvaluationSummaryItem> content = mapper.findList(periodId, status, size, offset);
        long totalCount = mapper.countList(periodId, status);
        long totalPages = (long) Math.ceil((double) totalCount / size);
        return new QuantitativeEvaluationListResponse(content, totalCount, totalPages);
    }

    public QuantitativeEvaluationDetailResponse getDetail(Long evaluationId) {
        QuantitativeEvaluationDetailResponse result = mapper.findById(evaluationId);
        if (result == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return result;
    }
}
