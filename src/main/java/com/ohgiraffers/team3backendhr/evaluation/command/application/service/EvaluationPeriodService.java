package com.ohgiraffers.team3backendhr.evaluation.command.application.service;

import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.evaluation.command.domain.aggregate.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.evaluation.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.evaluation.command.application.dto.request.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.evaluation.command.application.dto.request.EvaluationPeriodUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationPeriodService {

    private final EvaluationPeriodRepository repository;

    public void create(EvaluationPeriodCreateRequest request) {
        if (repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)) {
            throw new IllegalStateException("이미 진행 중인 평가 기간이 있습니다.");
        }
        EvaluationPeriod period = EvaluationPeriod.builder()
                .algorithmVersionId(request.algorithmVersionId())
                .evalYear(request.evalYear())
                .evalSequence(request.evalSequence())
                .evalType(request.evalType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();
        repository.save(period);
    }

    public void close(Long evalPeriodId) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("평가 기간을 찾을 수 없습니다."));
        period.close();
    }

    public void confirm(Long evalPeriodId) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("평가 기간을 찾을 수 없습니다."));
        period.confirm();
    }

    public void update(Long evalPeriodId, EvaluationPeriodUpdateRequest request) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("평가 기간을 찾을 수 없습니다."));
        period.update(request.startDate(), request.endDate(), request.algorithmVersionId());
    }
}
