package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationConfirmRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
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
public class QualitativeEvaluationService {

    private final QualitativeEvaluationRepository repository;
    private final AdminClient adminClient;
    private final IdGenerator idGenerator;

    /* 평가 기간 생성 시 WORKER × level 1·2·3 레코드 선생성 */
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

    /* 1차(TL) 임시저장 */
    public void saveDraft(Long evaluatorId, Long evaluateeId, QualitativeEvaluationDraftRequest request) {
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 1L);
        eval.saveDraft(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
    }

    /* 1차(TL) 제출 — grade 자동 산출 */
    public void submit(Long evaluatorId, Long evaluateeId, QualitativeEvaluationSubmitRequest request) {
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 1L);
        Grade grade = calculateGrade(request.getScore());
        eval.submit(evaluatorId, request.getEvalItems(), request.getEvalComment(),
                request.getInputMethod(), request.getScore(), grade);
    }

    /* 2차(DL) 임시저장 */
    public void saveDraftForDL(Long evaluatorId, Long evaluateeId, QualitativeEvaluationDraftRequest request) {
        validateLevel1Submitted(evaluateeId, request.getEvaluationPeriodId());
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 2L);
        eval.saveDraft(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
    }

    /* 2차(DL) 제출 — grade 자동 산출 */
    public void submitForDL(Long evaluatorId, Long evaluateeId, QualitativeEvaluationSubmitRequest request) {
        validateLevel1Submitted(evaluateeId, request.getEvaluationPeriodId());
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 2L);
        Grade grade = calculateGrade(request.getScore());
        eval.submit(evaluatorId, request.getEvalItems(), request.getEvalComment(),
                request.getInputMethod(), request.getScore(), grade);
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
}
