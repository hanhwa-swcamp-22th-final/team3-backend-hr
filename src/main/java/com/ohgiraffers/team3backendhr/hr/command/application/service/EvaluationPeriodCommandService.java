package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.evaluationperiod.EvaluationPeriodUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvalPeriodStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.AlgorithmVersionSnapshotResponse;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.EvaluationPeriodSnapshotEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.EvaluationReferenceEventPublisher;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationPeriodCommandService {

    private final EvaluationPeriodRepository repository;
    private final QualitativeEvaluationCommandService qualitativeEvaluationService;
    private final AdminClient adminClient;
    private final EvaluationReferenceEventPublisher evaluationReferenceEventPublisher;
    private final IdGenerator idGenerator;

    public void create(EvaluationPeriodCreateRequest request) {
        if (repository.existsByStatus(EvalPeriodStatus.IN_PROGRESS)) {
            throw new IllegalStateException("이미 진행 중인 평가 기간이 있습니다.");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 이후여야 합니다.");
        }
        if (repository.existsByEvalYearAndEvalSequenceAndEvalType(
                request.getEvalYear(), request.getEvalSequence(), request.getEvalType())) {
            throw new IllegalStateException("동일한 연도·차수·평가 유형의 평가 기간이 이미 존재합니다.");
        }
        if (repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                request.getEndDate(), request.getStartDate())) {
            throw new IllegalStateException("기존 평가 기간과 날짜가 중복됩니다.");
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
        publishPeriodSnapshotAfterCommit(period);
    }

    public void close(Long evalPeriodId) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("평가 기간을 찾을 수 없습니다."));
        period.close();
        publishPeriodSnapshotAfterCommit(period);
    }

    public void confirm(Long evalPeriodId) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("평가 기간을 찾을 수 없습니다."));
        period.confirm();
        publishPeriodSnapshotAfterCommit(period);
    }

    public void update(Long evalPeriodId, EvaluationPeriodUpdateRequest request) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("평가 기간을 찾을 수 없습니다."));

        LocalDate effectiveStart = request.getStartDate() != null ? request.getStartDate() : period.getStartDate();
        LocalDate effectiveEnd   = request.getEndDate()   != null ? request.getEndDate()   : period.getEndDate();

        if (!effectiveEnd.isAfter(effectiveStart)) {
            throw new IllegalArgumentException("종료일은 시작일보다 이후여야 합니다.");
        }
        if (repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndEvalPeriodIdNot(
                effectiveEnd, effectiveStart, evalPeriodId)) {
            throw new IllegalStateException("기존 평가 기간과 날짜가 중복됩니다.");
        }

        period.update(request.getStartDate(), request.getEndDate(), request.getAlgorithmVersionId());
        publishPeriodSnapshotAfterCommit(period);
    }

    private void publishPeriodSnapshotAfterCommit(EvaluationPeriod period) {
        EvaluationPeriodSnapshotEvent event = toSnapshotEvent(period);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evaluationReferenceEventPublisher.publishEvaluationPeriodSnapshot(event);
                }
            });
            return;
        }

        evaluationReferenceEventPublisher.publishEvaluationPeriodSnapshot(event);
    }

    private EvaluationPeriodSnapshotEvent toSnapshotEvent(EvaluationPeriod period) {
        AlgorithmVersionSnapshotResponse snapshot = adminClient.getAlgorithmVersionSnapshot(period.getAlgorithmVersionId());

        return EvaluationPeriodSnapshotEvent.builder()
            .evaluationPeriodId(period.getEvalPeriodId())
            .algorithmVersionId(period.getAlgorithmVersionId())
            .evaluationYear(period.getEvalYear())
            .evaluationSequence(period.getEvalSequence())
            .evaluationType(period.getEvalType().name())
            .startDate(period.getStartDate())
            .endDate(period.getEndDate())
            .status(period.getStatus().name())
            .algorithmVersionNo(snapshot != null ? snapshot.getVersionNo() : null)
            .algorithmImplementationKey(snapshot != null ? snapshot.getImplementationKey() : null)
            .parameters(snapshot != null ? snapshot.getParameters() : null)
            .referenceValues(snapshot != null ? snapshot.getReferenceValues() : null)
            .occurredAt(LocalDateTime.now())
            .build();
    }
}
