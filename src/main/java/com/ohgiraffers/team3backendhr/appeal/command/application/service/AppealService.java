package com.ohgiraffers.team3backendhr.appeal.command.application.service;

import com.ohgiraffers.team3backendhr.appeal.command.application.dto.request.AppealRegisterRequest;
import com.ohgiraffers.team3backendhr.appeal.command.application.dto.request.AppealReviewRequest;
import com.ohgiraffers.team3backendhr.appeal.command.application.dto.request.AppealUpdateRequest;
import com.ohgiraffers.team3backendhr.appeal.command.domain.aggregate.*;
import com.ohgiraffers.team3backendhr.appeal.command.domain.repository.AppealRepository;
import com.ohgiraffers.team3backendhr.appeal.command.domain.repository.AttachmentFileGroupRepository;
import com.ohgiraffers.team3backendhr.appeal.command.domain.repository.ScoreModificationLogRepository;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AppealService {

    private final AppealRepository appealRepository;
    private final AttachmentFileGroupRepository fileGroupRepository;
    private final ScoreModificationLogRepository scoreLogRepository;
    private final QualitativeEvaluationRepository qualitativeEvaluationRepository;
    private final IdGenerator idGenerator;

    /* 이의신청 등록 — 파일 그룹 자동 생성 (S3 추후 도입) */
    public void register(Long appealEmployeeId, AppealRegisterRequest request) {
        AttachmentFileGroup fileGroup = fileGroupRepository.save(
                AttachmentFileGroup.builder()
                        .fileGroupId(idGenerator.generate())
                        .referenceType(AttachmentFileGroup.ReferenceType.APPEAL)
                        .build());

        appealRepository.save(EvaluationAppeal.builder()
                .appealId(idGenerator.generate())
                .qualitativeEvaluationId(request.getQualitativeEvaluationId())
                .appealEmployeeId(appealEmployeeId)
                .appealType(request.getAppealType())
                .title(request.getTitle())
                .content(request.getContent())
                .filedAt(LocalDateTime.now())
                .fileGroupId(fileGroup.getFileGroupId())
                .build());
    }

    /* 이의신청 수정 */
    public void update(Long appealId, Long requesterId, AppealUpdateRequest request) {
        EvaluationAppeal appeal = findAppeal(appealId);
        validateOwner(appeal, requesterId, "본인의 이의신청만 수정할 수 있습니다.");
        appeal.update(request.getAppealType(), request.getTitle(), request.getContent());
    }

    /* 이의신청 취소 — 논리 삭제 대신 물리 삭제 */
    public void cancel(Long appealId, Long requesterId) {
        EvaluationAppeal appeal = findAppeal(appealId);
        validateOwner(appeal, requesterId, "본인의 이의신청만 취소할 수 있습니다.");
        appeal.cancel();    // COMPLETED 상태 검증
        appealRepository.delete(appeal);
    }

    /* 승인 — score_modification_log 생성 + 평가 점수 반영 */
    public void approve(Long appealId, Long reviewerId, AppealReviewRequest request) {
        EvaluationAppeal appeal = findAppeal(appealId);
        appeal.approve(reviewerId, request.getReviewResult());

        if (request.getModifiedScore() != null) {
            QualitativeEvaluation eval = qualitativeEvaluationRepository
                    .findById(appeal.getQualitativeEvaluationId())
                    .orElseThrow(() -> new IllegalArgumentException("평가를 찾을 수 없습니다."));

            scoreLogRepository.save(ScoreModificationLog.builder()
                    .scoreModificationLogId(idGenerator.generate())
                    .scoreEvaluateeId(eval.getEvaluateeId())
                    .scoreModifierId(reviewerId)
                    .scoreOriginalScore(eval.getScore())
                    .scoreModifiedScore(request.getModifiedScore())
                    .scoreReason(request.getReason())
                    .scoreModifiedAt(LocalDateTime.now())
                    .build());
        }
    }

    /* 반려 */
    public void reject(Long appealId, Long reviewerId) {
        findAppeal(appealId).reject(reviewerId);
    }

    /* 보류 */
    public void hold(Long appealId, Long reviewerId) {
        findAppeal(appealId).hold();
    }

    private EvaluationAppeal findAppeal(Long appealId) {
        return appealRepository.findById(appealId)
                .orElseThrow(() -> new IllegalArgumentException("이의신청을 찾을 수 없습니다."));
    }

    private void validateOwner(EvaluationAppeal appeal, Long requesterId, String message) {
        if (!appeal.getAppealEmployeeId().equals(requesterId)) {
            throw new IllegalStateException(message);
        }
    }
}
