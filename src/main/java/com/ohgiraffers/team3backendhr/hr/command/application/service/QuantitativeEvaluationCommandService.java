package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantEvalScores;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation.QuantitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QuantitativeEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuantitativeEvaluationCommandService {

    private final QuantitativeEvaluationRepository repository;
    private final IdGenerator idGenerator;

    /** 배치 계산 결과 반영 — 기존 레코드 있으면 UPDATE, 없으면 INSERT */
    public void applyBatchResult(Long employeeId, Long evalPeriodId, Long equipmentId, QuantEvalScores scores) {
        QuantitativeEvaluation eval = repository.findByEmployeeIdAndEvalPeriodId(employeeId, evalPeriodId)
                .orElseGet(() -> QuantitativeEvaluation.builder()
                        .quantitativeEvaluationId(idGenerator.generate())
                        .employeeId(employeeId)
                        .evalPeriodId(evalPeriodId)
                        .equipmentId(equipmentId)
                        .status(QuantEvalStatus.TEMPORARY)
                        .build());

        eval.applyBatchResult(scores);
        repository.save(eval);
    }

    /** HRM 최종 확정 */
    public void confirm(Long quantitativeEvaluationId) {
        QuantitativeEvaluation eval = repository.findById(quantitativeEvaluationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
        eval.confirm();
        repository.save(eval);
    }
}
