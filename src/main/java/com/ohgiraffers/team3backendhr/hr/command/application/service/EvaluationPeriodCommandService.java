package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationPeriodCommandService {

    private final EvaluationPeriodRepository repository;
    private final QualitativeEvaluationCommandService qualitativeEvaluationService;
    private final IdGenerator idGenerator;

    public void create(EvaluationPeriodCreateRequest request) {
        if (repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_ALREADY_IN_PROGRESS);
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
        if (repository.existsByEvalYearAndEvalSequenceAndEvalType(
                request.getEvalYear(), request.getEvalSequence(), request.getEvalType())) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_DUPLICATE);
        }
        if (repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                request.getEndDate(), request.getStartDate())) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_DATE_OVERLAP);
        }
        EvaluationPeriod period = EvaluationPeriod.builder()
                .evalPeriodId(idGenerator.generate())
                .algorithmVersionId(request.getAlgorithmVersionId())
                .evalYear(request.getEvalYear())
                .evalSequence(request.getEvalSequence())
                .evalType(request.getEvalType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        repository.save(period);
        qualitativeEvaluationService.createRecordsForPeriod(period.getEvalPeriodId());
    }

    public void close(Long evalPeriodId) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVAL_PERIOD_NOT_FOUND));
        period.close();
    }

    public void confirm(Long evalPeriodId) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVAL_PERIOD_NOT_FOUND));
        period.confirm();
    }

    public void update(Long evalPeriodId, EvaluationPeriodUpdateRequest request) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVAL_PERIOD_NOT_FOUND));

        LocalDate effectiveStart = request.getStartDate() != null ? request.getStartDate() : period.getStartDate();
        LocalDate effectiveEnd   = request.getEndDate()   != null ? request.getEndDate()   : period.getEndDate();

        if (!effectiveEnd.isAfter(effectiveStart)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
        if (repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndEvalPeriodIdNot(
                effectiveEnd, effectiveStart, evalPeriodId)) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_DATE_OVERLAP);
        }

        period.update(request.getStartDate(), request.getEndDate(), request.getAlgorithmVersionId());
    }
}
