package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.EvaluationPeriodUpdateRequest;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationPeriodService {

    private final EvaluationPeriodRepository repository;
    private final QualitativeEvaluationRepository qualitativeEvaluationRepository;
    private final AdminClient adminClient;
    private final IdGenerator idGenerator;

    public void create(EvaluationPeriodCreateRequest request) {
        if (repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)) {
            throw new IllegalStateException("이미 진행 중인 평가 기간이 있습니다.");
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

        // Admin 서비스에서 WORKER 목록 조회 후 level 1·2·3 레코드 선생성
        List<WorkerResponse> workers = adminClient.getWorkers();
        List<QualitativeEvaluation> evaluations = new ArrayList<>();
        for (WorkerResponse worker : workers) {
            for (long level = 1; level <= 3; level++) {
                evaluations.add(QualitativeEvaluation.builder()
                        .qualitativeEvaluationId(idGenerator.generate())
                        .evaluateeId(worker.getEmployeeId())
                        .evaluationPeriodId(period.getEvalPeriodId())
                        .evaluationLevel(level)
                        .build());
            }
        }
        qualitativeEvaluationRepository.saveAll(evaluations);
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
        period.update(request.getStartDate(), request.getEndDate(), request.getAlgorithmVersionId());
    }
}
