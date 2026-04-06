package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationConfirmRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationSubmittedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.QualitativeEvaluationEventPublisher;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class QualitativeEvaluationCommandService {

    private final QualitativeEvaluationRepository repository;
    private final AdminClient adminClient;
    private final IdGenerator idGenerator;
    private final QualitativeEvaluationEventPublisher qualitativeEvaluationEventPublisher;

    public void createRecordsForPeriod(Long periodId) {
        List<WorkerResponse> workers = adminClient.getWorkers();
        List<QualitativeEvaluation> evaluations = new ArrayList<>();
        for (WorkerResponse worker : workers) {
            for (long level = 1; level <= 3; level++) {
                evaluations.add(QualitativeEvaluation.builder()
                    .qualitativeEvaluationId(idGenerator.generate())
                    .evaluateeId(worker.getEmployeeId())
                    .evaluationPeriodId(periodId)
                    .evaluationLevel(level)
                    .build());
            }
        }
        repository.saveAll(evaluations);
    }

    public void saveDraft(Long evaluatorId, Long evaluateeId, QualitativeEvaluationDraftRequest request) {
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 1L);
        eval.saveDraft(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
    }

    public void submit(Long evaluatorId, Long evaluateeId, QualitativeEvaluationSubmitRequest request) {
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 1L);
        Grade grade = calculateGrade(request.getScore());
        eval.submit(
            evaluatorId,
            request.getEvalItems(),
            request.getEvalComment(),
            request.getInputMethod(),
            request.getScore(),
            grade
        );
        publishSubmittedEventAfterCommit(eval);
    }

    public void saveDraftForDL(Long evaluatorId, Long evaluateeId, QualitativeEvaluationDraftRequest request) {
        validateLevel1Submitted(evaluateeId, request.getEvaluationPeriodId());
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 2L);
        eval.saveDraft(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
    }

    public void submitForDL(Long evaluatorId, Long evaluateeId, QualitativeEvaluationSubmitRequest request) {
        validateLevel1Submitted(evaluateeId, request.getEvaluationPeriodId());
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 2L);
        Grade grade = calculateGrade(request.getScore());
        eval.submit(
            evaluatorId,
            request.getEvalItems(),
            request.getEvalComment(),
            request.getInputMethod(),
            request.getScore(),
            grade
        );
        publishSubmittedEventAfterCommit(eval);
    }

    /* 3차(HRM) 최종 확정 — 2차 제출 여부 검증 후 CONFIRMED */
    public void confirmFinal(Long evaluatorId, Long evaluateeId, QualitativeEvaluationConfirmRequest request) {
        validateLevel2Submitted(evaluateeId, request.getEvaluationPeriodId());
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 3L);
        eval.confirmFinal(evaluatorId, request.getEvalComment(), request.getInputMethod());
    }

    private void validateLevel2Submitted(Long evaluateeId, Long evaluationPeriodId) {
        QualitativeEvaluation level2 = findByLevel(evaluateeId, evaluationPeriodId, 2L);
        if (level2.getStatus() != QualEvalStatus.SUBMITTED) {
            throw new IllegalStateException("2차 평가가 제출되지 않아 최종 확정을 진행할 수 없습니다.");
        }
    }

    private void validateLevel1Submitted(Long evaluateeId, Long evaluationPeriodId) {
        QualitativeEvaluation level1 = findByLevel(evaluateeId, evaluationPeriodId, 1L);
        if (level1.getStatus() != QualEvalStatus.SUBMITTED) {
            throw new IllegalStateException("1차 평가가 제출되지 않아 2차 평가를 진행할 수 없습니다.");
        }
    }

    private QualitativeEvaluation findByLevel(Long evaluateeId, Long evaluationPeriodId, Long level) {
        return repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                evaluateeId, evaluationPeriodId, level)
            .orElseThrow(() -> new IllegalArgumentException("평가 레코드를 찾을 수 없습니다."));
    }

    private Grade calculateGrade(Double score) {
        if (score >= 90) return Grade.S;
        if (score >= 80) return Grade.A;
        if (score >= 70) return Grade.B;
        return Grade.C;
    }

    private void publishSubmittedEventAfterCommit(QualitativeEvaluation evaluation) {
        QualitativeEvaluationSubmittedEvent event = new QualitativeEvaluationSubmittedEvent(
            evaluation.getQualitativeEvaluationId(),
            evaluation.getEvaluationPeriodId(),
            evaluation.getEvaluateeId(),
            evaluation.getEvaluatorId(),
            evaluation.getEvaluationLevel(),
            evaluation.getStatus().name(),
            LocalDateTime.now()
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    qualitativeEvaluationEventPublisher.publishSubmitted(event);
                }
            });
            return;
        }

        qualitativeEvaluationEventPublisher.publishSubmitted(event);
    }
}