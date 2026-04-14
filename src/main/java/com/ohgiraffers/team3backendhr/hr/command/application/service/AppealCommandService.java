package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealRegisterRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealProcessRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealReviewRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealStatusUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.AppealStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.ReviewResult;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachmentfilegroup.AttachmentFileGroup;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachmentfilegroup.ReferenceType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.EvaluationAppeal;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.scoremodificationlog.ScoreModificationLog;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.AppealRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.AttachmentFileGroupRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.ScoreModificationLogRepository;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notification.NotificationType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AppealCommandService {

    private final AppealRepository appealRepository;
    private final AttachmentFileGroupRepository fileGroupRepository;
    private final ScoreModificationLogRepository scoreLogRepository;
    private final QualitativeEvaluationRepository qualitativeEvaluationRepository;
    private final IdGenerator idGenerator;
    private final NotificationCommandService notificationCommandService;

    /* 이의신청 등록 — 2차 평가 제출 후 7일 이내, 평가기간당 1회 */
    public void register(Long appealEmployeeId, AppealRegisterRequest request) {
        if (appealRepository.existsByAppealEmployeeIdAndEvaluationPeriodId(
                appealEmployeeId, request.getEvaluationPeriodId())) {
            throw new BusinessException(ErrorCode.APPEAL_ALREADY_EXISTS);
        }

        List<QualitativeEvaluation> evaluations = findAppealTargetEvaluations(
            appealEmployeeId,
            request.getEvaluationPeriodId()
        );

        boolean appealEligible = evaluations.stream().anyMatch(eval ->
            eval.getEvaluationLevel() == 2L
                && eval.getStatus() == QualEvalStatus.SUBMITTED
        );
        if (!appealEligible) {
            throw new BusinessException(ErrorCode.EVALUATION_LEVEL2_NOT_SUBMITTED_FOR_APPEAL);
        }

        LocalDateTime latestSubmittedAt = evaluations.stream()
            .filter(eval -> eval.getEvaluationLevel() == 2L)
            .filter(eval -> eval.getStatus() == QualEvalStatus.SUBMITTED)
            .map(QualitativeEvaluation::getUpdatedAt)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        if (latestSubmittedAt == null || latestSubmittedAt.plusDays(7).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.APPEAL_EXPIRED);
        }

        Long appealId = idGenerator.generate();

        AttachmentFileGroup fileGroup = fileGroupRepository.save(
                AttachmentFileGroup.builder()
                        .fileGroupId(idGenerator.generate())
                        .referenceType(ReferenceType.APPEAL)
                        .build());
        fileGroup.assignReference(appealId);

        appealRepository.save(EvaluationAppeal.builder()
                .appealId(appealId)
                .appealEmployeeId(appealEmployeeId)
                .evaluationPeriodId(request.getEvaluationPeriodId())
                .appealType(request.getAppealType())
                .title(request.getTitle())
                .content(request.getContent())
                .anonymizedComparison(evaluations.get(0).getQualitativeEvaluationId())
                .filedAt(LocalDateTime.now())
                .fileGroupId(fileGroup.getFileGroupId())
                .build());

        List<Long> recipients = evaluations.stream()
            .map(QualitativeEvaluation::getEvaluatorId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (!recipients.isEmpty()) {
            notificationCommandService.create(
                NotificationType.OBJECTIONS,
                "이의신청 접수",
                appealEmployeeId + "번 직원이 이의신청을 제출했습니다.",
                recipients
            );
        }
    }

    /* 이의신청 수정 */
    public void update(Long appealId, Long requesterId, AppealUpdateRequest request) {
        EvaluationAppeal appeal = findAppeal(appealId);
        validateOwner(appeal, requesterId, "본인의 이의신청만 수정할 수 있습니다.");
        throw new BusinessException(ErrorCode.INVALID_INPUT, "제출된 이의신청은 수정할 수 없습니다.");
    }

    /* 이의신청 취소 — 논리 삭제 대신 물리 삭제 */
    public void cancel(Long appealId, Long requesterId) {
        EvaluationAppeal appeal = findAppeal(appealId);
        validateOwner(appeal, requesterId, "본인의 이의신청만 취소할 수 있습니다.");
        appeal.cancel();    // COMPLETED 상태 검증
        appealRepository.delete(appeal);
    }

    /* HRM 상태 변경 — 보류·승인·반려 통합 처리 */
    public void updateStatus(Long appealId, Long reviewerId, AppealStatusUpdateRequest request) {
        EvaluationAppeal appeal = findAppeal(appealId);

        if (request.getStatus() == AppealStatus.RECEIVING) {
            appeal.receive(reviewerId);
            return;
        }
        if (request.getStatus() == AppealStatus.REVIEWING) {
            ensureReviewStarted(appeal, reviewerId);
            appeal.hold();
            return;
        }
        // COMPLETED
        ReviewResult reviewResult = request.getReviewResult();
        if (reviewResult == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "COMPLETED 상태에는 reviewResult가 필요합니다.");
        }
        if (reviewResult == ReviewResult.DISMISS) {
            requireStatus(appeal, AppealStatus.SUBMITTED);
            appeal.reject(reviewerId);
        } else {
            requireStatus(appeal, AppealStatus.REVIEWING);
            approve(appeal, reviewerId,
                new AppealReviewRequest(reviewResult, request.getModifiedScore(), request.getReason()));
        }
    }

    public void receiveByHrm(Long appealId, Long reviewerId) {
        EvaluationAppeal appeal = findAppeal(appealId);
        requireStatus(appeal, AppealStatus.SUBMITTED);
        reopenAppealTargetEvaluations(appeal);
        appeal.receive(reviewerId);
    }

    public void rejectByHrm(Long appealId, Long reviewerId, String reason) {
        EvaluationAppeal appeal = findAppeal(appealId);
        requireStatus(appeal, AppealStatus.SUBMITTED);
        appeal.reject(reviewerId);
    }

    public void approveByTl(Long appealId, Long reviewerId, AppealProcessRequest request) {
        EvaluationAppeal appeal = findAppeal(appealId);
        requireStatus(appeal, AppealStatus.RECEIVING);
        validateAssignedReviewer(appeal, reviewerId, 1L);
        appeal.startReview(reviewerId);
    }

    public void rejectByTl(Long appealId, Long reviewerId, String reason) {
        EvaluationAppeal appeal = findAppeal(appealId);
        requireStatus(appeal, AppealStatus.RECEIVING);
        validateAssignedReviewer(appeal, reviewerId, 1L);
        appeal.reject(reviewerId);
    }

    public void approveByDl(Long appealId, Long reviewerId, AppealProcessRequest request) {
        EvaluationAppeal appeal = findAppeal(appealId);
        requireStatus(appeal, AppealStatus.REVIEWING);
        validateAssignedReviewer(appeal, reviewerId, 2L);
        approve(appeal, reviewerId,
            new AppealReviewRequest(ReviewResult.ACKNOWLEDGE, request.getModifiedScore(), request.getReason()));
    }

    public void rejectByDl(Long appealId, Long reviewerId, String reason) {
        EvaluationAppeal appeal = findAppeal(appealId);
        requireStatus(appeal, AppealStatus.REVIEWING);
        validateAssignedReviewer(appeal, reviewerId, 2L);
        approve(appeal, reviewerId,
            new AppealReviewRequest(ReviewResult.ACKNOWLEDGE_IN_PART, null, reason));
    }

    /* 승인 — score_modification_log 생성 + 평가 점수 반영 */
    public void approve(Long appealId, Long reviewerId, AppealReviewRequest request) {
        EvaluationAppeal appeal = findAppeal(appealId);
        approve(appeal, reviewerId, request);
    }

    private void approve(EvaluationAppeal appeal, Long reviewerId, AppealReviewRequest request) {
        appeal.approve(reviewerId, request.getReviewResult());

        if (request.getModifiedScore() != null) {
            List<QualitativeEvaluation> evaluations = findAppealTargetEvaluations(
                appeal.getAppealEmployeeId(),
                appeal.getEvaluationPeriodId()
            );
            double originalAverage = evaluations.stream()
                .map(QualitativeEvaluation::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

            scoreLogRepository.save(ScoreModificationLog.builder()
                    .scoreModificationLogId(idGenerator.generate())
                    .scoreEvaluateeId(appeal.getAppealEmployeeId())
                    .scoreModifierId(reviewerId)
                    .scoreOriginalScore(originalAverage)
                    .scoreModifiedScore(request.getModifiedScore())
                    .scoreReason(request.getReason())
                    .scoreModifiedAt(LocalDateTime.now())
                    .build());
        }
    }

    /* 반려 */
    public void reject(Long appealId, Long reviewerId) {
        EvaluationAppeal appeal = findAppeal(appealId);
        appeal.reject(reviewerId);
    }

    /* 보류 */
    public void hold(Long appealId, Long reviewerId) {
        EvaluationAppeal appeal = findAppeal(appealId);
        ensureReviewStarted(appeal, reviewerId);
        appeal.hold();
    }

    private EvaluationAppeal findAppeal(Long appealId) {
        return appealRepository.findById(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPEAL_NOT_FOUND));
    }

    private void validateOwner(EvaluationAppeal appeal, Long requesterId, String message) {
        if (!appeal.getAppealEmployeeId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, message);
        }
    }

    private void requireStatus(EvaluationAppeal appeal, AppealStatus expectedStatus) {
        if (appeal.getStatus() != expectedStatus) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "현재 단계에서 처리할 수 없는 이의신청입니다.");
        }
    }

    private void ensureReviewStarted(EvaluationAppeal appeal, Long reviewerId) {
        if (appeal.getStatus() == AppealStatus.SUBMITTED) {
            appeal.receive(reviewerId);
        }
        if (appeal.getStatus() == AppealStatus.RECEIVING) {
            appeal.startReview(reviewerId);
        }
    }

    private void validateAssignedReviewer(EvaluationAppeal appeal, Long reviewerId, Long evaluationLevel) {
        QualitativeEvaluation evaluation = qualitativeEvaluationRepository
            .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(
                appeal.getAppealEmployeeId(),
                appeal.getEvaluationPeriodId(),
                evaluationLevel
            )
            .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));

        if (!Objects.equals(evaluation.getEvaluatorId(), reviewerId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "담당 평가자만 처리할 수 있습니다.");
        }
    }

    private void reopenAppealTargetEvaluations(EvaluationAppeal appeal) {
        findAppealTargetEvaluations(appeal.getAppealEmployeeId(), appeal.getEvaluationPeriodId())
            .forEach(QualitativeEvaluation::reopenForAppealReview);
    }

    private List<QualitativeEvaluation> findAppealTargetEvaluations(Long employeeId, Long evaluationPeriodId) {
        List<QualitativeEvaluation> evaluations = new ArrayList<>();
        qualitativeEvaluationRepository
            .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(employeeId, evaluationPeriodId, 1L)
            .ifPresent(evaluations::add);
        qualitativeEvaluationRepository
            .findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(employeeId, evaluationPeriodId, 2L)
            .ifPresent(evaluations::add);

        if (evaluations.isEmpty()) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND);
        }
        return evaluations;
    }
}
