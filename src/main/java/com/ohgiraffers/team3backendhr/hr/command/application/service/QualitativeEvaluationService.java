package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Grade;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QualitativeEvaluationService {

    private final QualitativeEvaluationRepository repository;

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
