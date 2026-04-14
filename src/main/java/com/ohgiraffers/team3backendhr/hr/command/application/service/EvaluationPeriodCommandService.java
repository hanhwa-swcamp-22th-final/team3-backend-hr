package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
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
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationPeriodCommandService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationPeriodCommandService.class);

    private final EvaluationPeriodRepository repository;
    private final QualitativeEvaluationCommandService qualitativeEvaluationService;
    private final AdminClient adminClient;
    private final EvaluationReferenceEventPublisher evaluationReferenceEventPublisher;
    private final IdGenerator idGenerator;

    public void create(EvaluationPeriodCreateRequest request) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
        if (repository.existsByEvalYearAndEvalSequence(
                request.getEvalYear(), request.getEvalSequence())) {
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
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        repository.save(period);
        qualitativeEvaluationService.createRecordsForPeriod(period.getEvalPeriodId());
        publishPeriodSnapshotAfterCommit(period);
    }

    public void delete(Long evalPeriodId) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVAL_PERIOD_NOT_FOUND));
        qualitativeEvaluationService.deleteByPeriodId(evalPeriodId);
        repository.delete(period);
    }

    public void close(Long evalPeriodId) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVAL_PERIOD_NOT_FOUND));
        period.close();
        publishPeriodSnapshotAfterCommit(period);
    }

    public void confirm(Long evalPeriodId) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVAL_PERIOD_NOT_FOUND));
        period.confirm();
        publishPeriodSnapshotAfterCommit(period);
    }

    public void update(Long evalPeriodId, EvaluationPeriodUpdateRequest request) {
        EvaluationPeriod period = repository.findById(evalPeriodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVAL_PERIOD_NOT_FOUND));

        LocalDate effectiveStart = request.getStartDate() != null ? request.getStartDate() : period.getStartDate();
        LocalDate effectiveEnd = request.getEndDate() != null ? request.getEndDate() : period.getEndDate();

        if (!effectiveEnd.isAfter(effectiveStart)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
        if (repository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndEvalPeriodIdNot(
                effectiveEnd, effectiveStart, evalPeriodId)) {
            throw new BusinessException(ErrorCode.EVAL_PERIOD_DATE_OVERLAP);
        }

        period.update(request.getStartDate(), request.getEndDate(), request.getAlgorithmVersionId());
        publishPeriodSnapshotAfterCommit(period);
    }

    @Transactional(readOnly = true)
    public int republishSnapshots() {
        var periods = repository.findAll().stream()
            .sorted(Comparator.comparing(EvaluationPeriod::getEndDate)
                .thenComparing(EvaluationPeriod::getEvalPeriodId))
            .toList();

        periods.forEach(this::publishPeriodSnapshot);
        log.info("Republished evaluation period snapshots. count={}", periods.size());
        return periods.size();
    }

    private void publishPeriodSnapshotAfterCommit(EvaluationPeriod period) {
        EvaluationPeriodSnapshotEvent event = toSnapshotEvent(period);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishPeriodSnapshot(event);
                }
            });
            return;
        }

        publishPeriodSnapshot(event);
    }

    private void publishPeriodSnapshot(EvaluationPeriod period) {
        publishPeriodSnapshot(toSnapshotEvent(period));
    }

    private void publishPeriodSnapshot(EvaluationPeriodSnapshotEvent event) {
        evaluationReferenceEventPublisher.publishEvaluationPeriodSnapshot(event);
    }

    private EvaluationPeriodSnapshotEvent toSnapshotEvent(EvaluationPeriod period) {
        AlgorithmVersionSnapshotResponse snapshot = null;
        try {
            snapshot = adminClient.getAlgorithmVersionSnapshot(period.getAlgorithmVersionId());
        } catch (Exception ex) {
            log.warn(
                "Failed to load algorithm version snapshot for evaluation period {}. The evaluation period snapshot will be published without algorithm metadata.",
                period.getEvalPeriodId(),
                ex
            );
        }

        return EvaluationPeriodSnapshotEvent.builder()
            .evaluationPeriodId(period.getEvalPeriodId())
            .algorithmVersionId(period.getAlgorithmVersionId())
            .evaluationYear(period.getEvalYear())
            .evaluationSequence(period.getEvalSequence())
            .evaluationType(null)
            .startDate(period.getStartDate())
            .endDate(period.getEndDate())
            .status(period.getStatus().name())
            .algorithmVersionNo(snapshot != null ? snapshot.getVersionNo() : null)
            .algorithmImplementationKey(snapshot != null ? snapshot.getImplementationKey() : null)
            .policyConfig(snapshot != null ? snapshot.getPolicyConfig() : null)
            .parameters(snapshot != null ? snapshot.getParameters() : null)
            .referenceValues(snapshot != null ? snapshot.getReferenceValues() : null)
            .occurredAt(LocalDateTime.now())
            .build();
    }
}
