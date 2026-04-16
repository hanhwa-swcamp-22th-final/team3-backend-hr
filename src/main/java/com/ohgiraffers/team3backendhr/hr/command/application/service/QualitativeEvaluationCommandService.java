package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationConfirmRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcomment.EvaluationComment;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.scoremodificationlog.ScoreModificationLog;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationCommentRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.ScoreModificationLogRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DomainKeywordRuleResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationAnalyzedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationNormalizedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationSubmittedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeKeywordRuleEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.MatchedKeywordDetailEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeSentenceAnalysisEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.QualitativeEvaluationEventPublisher;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notification.NotificationType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.tierconfig.Grade;

@Service
@RequiredArgsConstructor
@Transactional
public class QualitativeEvaluationCommandService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final String DEFAULT_ANALYSIS_VERSION = "squal-v1";

    private final QualitativeEvaluationRepository repository;
    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final EvaluationCommentRepository evaluationCommentRepository;
    private final AdminClient adminClient;
    private final IdGenerator idGenerator;
    private final ScoreModificationLogRepository scoreLogRepository;
    private final QualitativeEvaluationEventPublisher qualitativeEvaluationEventPublisher;
    private final ObjectMapper objectMapper;
    private final NotificationCommandService notificationCommandService;

    public void deleteByPeriodId(Long periodId) {
        repository.deleteByEvaluationPeriodId(periodId);
    }

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
        eval.submit(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
        publishSubmittedEventAfterCommit(eval);
    }

    /* TL — draft·submit 통합 (PATCH) */
    public void updateForTL(Long evaluatorId, Long evaluateeId, QualitativeEvaluationUpdateRequest request) {
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 1L);
        if (request.getStatus() == QualEvalStatus.DRAFT) {
            eval.saveDraft(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
        } else if (request.getStatus() == QualEvalStatus.SUBMITTED) {
            validateEvalComment(request.getEvalComment());
            eval.submit(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
            publishSubmittedEventAfterCommit(eval);
        } else {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "TL 평가는 DRAFT 또는 SUBMITTED만 허용됩니다.");
        }
    }

    /* DL — draft·submit 통합 (PATCH) */
    public void updateForDL(Long evaluatorId, Long evaluateeId, QualitativeEvaluationUpdateRequest request) {
        validateLevel1Submitted(evaluateeId, request.getEvaluationPeriodId());
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 2L);
        if (request.getStatus() == QualEvalStatus.DRAFT) {
            eval.saveDraft(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
        } else if (request.getStatus() == QualEvalStatus.SUBMITTED) {
            validateDlSubmitComment(request.getEvalComment());
            eval.submit(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
            publishSubmittedEventAfterCommit(eval);
        } else {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "DL 평가는 DRAFT 또는 SUBMITTED만 허용됩니다.");
        }
    }

    private void validateEvalComment(String evalComment) {
        if (evalComment == null || evalComment.trim().length() < 20) {
            throw new BusinessException(ErrorCode.INVALID_COMMENT_LENGTH);
        }
    }

    public void saveDraftForDL(Long evaluatorId, Long evaluateeId, QualitativeEvaluationDraftRequest request) {
        validateLevel1Submitted(evaluateeId, request.getEvaluationPeriodId());
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 2L);
        eval.saveDraft(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
    }

    public void submitForDL(Long evaluatorId, Long evaluateeId, QualitativeEvaluationSubmitRequest request) {
        validateLevel1Submitted(evaluateeId, request.getEvaluationPeriodId());
        validateDlSubmitComment(request.getEvalComment());
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 2L);
        eval.submit(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
        publishSubmittedEventAfterCommit(eval);
    }

    private void validateDlSubmitComment(String evalComment) {
        if (evalComment == null || evalComment.trim().isEmpty()) {
            return;
        }
        validateEvalComment(evalComment);
    }

    public void applyAnalyzedResult(QualitativeEvaluationAnalyzedEvent event) {
        BigDecimal rawScore = requireValue(event.getRawScore(), "rawScore");

        QualitativeEvaluation eval = repository.findById(event.getQualitativeEvaluationId())
            .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
        eval.applyAnalysisResult(rawScore.doubleValue());
        eval.updateEvalItems(buildEvalItemsFromAnalyzedEvent(event, rawScore));
        replaceEvaluationComments(event);

        if (eval.getEvaluatorId() != null) {
            notificationCommandService.create(
                NotificationType.RESULTS,
                "평가 분석 결과 도착",
                eval.getEvaluateeId() + "번 직원의 정성 평가 분석이 완료되었습니다.",
                List.of(eval.getEvaluatorId())
            );
        }
    }

    public void applyNormalizedResult(QualitativeEvaluationNormalizedEvent event) {
        BigDecimal sQual = requireValue(event.getSQual(), "sQual");
        String gradeValue = requireValue(event.getGrade(), "grade");

        QualitativeEvaluation eval = repository.findById(event.getQualitativeEvaluationId())
            .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
        eval.applyNormalizationResult(sQual.doubleValue(), Grade.valueOf(gradeValue));
    }

    public void confirmFinal(Long evaluatorId, Long evaluateeId, QualitativeEvaluationConfirmRequest request) {
        validateLevel2Submitted(evaluateeId, request.getEvaluationPeriodId());
        QualitativeEvaluation level1 = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 1L);
        QualitativeEvaluation level2 = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 2L);
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 3L);
        Double originalLevel3Score = eval.getScore();
        Double modifiedLevel3Score = BigDecimal.valueOf(
                requireScore(level2, "2차")
        ).setScale(4, RoundingMode.HALF_UP).doubleValue();

        eval.updateScore(modifiedLevel3Score);
        level1.confirmFinal(level1.getEvaluatorId(), level1.getEvalComment(), level1.getInputMethod());
        level2.confirmFinal(level2.getEvaluatorId(), level2.getEvalComment(), level2.getInputMethod());
        eval.confirmFinal(evaluatorId, request.getEvalComment(), request.getInputMethod());
        scoreLogRepository.save(ScoreModificationLog.builder()
                .scoreModificationLogId(idGenerator.generate())
                .scoreEvaluateeId(evaluateeId)
                .scoreModifierId(evaluatorId)
                .scoreOriginalScore(originalLevel3Score)
                .scoreModifiedScore(modifiedLevel3Score)
                .scoreReason(request.getEvalComment())
                .scoreModifiedAt(LocalDateTime.now())
                .build());
    }

    private void replaceEvaluationComments(QualitativeEvaluationAnalyzedEvent event) {
        evaluationCommentRepository.deleteByQualitativeEvaluationId(event.getQualitativeEvaluationId());
        if (event.getSentenceAnalyses() == null || event.getSentenceAnalyses().isEmpty()) {
            return;
        }

        Long algorithmVersionId = requireValue(event.getAlgorithmVersionId(), "algorithmVersionId");
        LocalDateTime occurredAt = event.getAnalyzedAt() != null ? event.getAnalyzedAt() : LocalDateTime.now();

        List<EvaluationComment> comments = event.getSentenceAnalyses().stream()
            .map(sentenceAnalysis -> toEvaluationComment(event.getQualitativeEvaluationId(), algorithmVersionId, occurredAt, sentenceAnalysis))
            .toList();
        evaluationCommentRepository.saveAll(comments);
    }

    private EvaluationComment toEvaluationComment(
        Long qualitativeEvaluationId,
        Long algorithmVersionId,
        LocalDateTime occurredAt,
        QualitativeSentenceAnalysisEvent sentenceAnalysis
    ) {
        return EvaluationComment.builder()
            .evaluationCommentId(idGenerator.generate())
            .qualitativeEvaluationId(qualitativeEvaluationId)
            .algorithmVersionId(algorithmVersionId)
            .nlpSentiment(sentenceAnalysis.getNlpSentiment())
            .matchedKeywordCount(sentenceAnalysis.getMatchedKeywordCount() == null ? 0 : sentenceAnalysis.getMatchedKeywordCount())
            .matchedKeywords(toJson(sentenceAnalysis.getMatchedKeywords()))
            .matchedKeywordDetails(toJson(sentenceAnalysis.getMatchedKeywordDetails()))
            .contextWeight(sentenceAnalysis.getContextWeight())
            .negationDetected(Boolean.TRUE.equals(sentenceAnalysis.getNegationDetected()))
            .createdAt(occurredAt)
            .updatedAt(occurredAt)
            .build();
    }

    private String buildEvalItemsFromAnalyzedEvent(QualitativeEvaluationAnalyzedEvent event, BigDecimal rawScore) {
        Map<String, BigDecimal> categoryWeights = new LinkedHashMap<>();
        if (event.getSentenceAnalyses() != null) {
            for (QualitativeSentenceAnalysisEvent sentenceAnalysis : event.getSentenceAnalyses()) {
                if (sentenceAnalysis == null || sentenceAnalysis.getMatchedKeywordDetails() == null) {
                    continue;
                }
                for (MatchedKeywordDetailEvent detail : sentenceAnalysis.getMatchedKeywordDetails()) {
                    if (detail == null) {
                        continue;
                    }
                    String category = normalizeDomainCategory(detail.getDomainCompetencyCategory());
                    if (category == null) {
                        continue;
                    }
                    BigDecimal weight = detail.getScoreWeight() == null
                        ? BigDecimal.ZERO
                        : detail.getScoreWeight().max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP);
                    if (weight.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    categoryWeights.merge(category, weight, BigDecimal::add);
                }
            }
        }

        if (categoryWeights.isEmpty()) {
            return "{}";
        }

        BigDecimal totalWeight = categoryWeights.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return "{}";
        }

        BigDecimal displayScore = rawScore.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal assigned = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int lastIndex = categoryWeights.size() - 1;
        int currentIndex = 0;
        Map<String, BigDecimal> evalItems = new LinkedHashMap<>();

        for (Map.Entry<String, BigDecimal> entry : categoryWeights.entrySet()) {
            BigDecimal value;
            if (currentIndex == lastIndex) {
                value = displayScore.subtract(assigned).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            } else {
                value = displayScore.multiply(entry.getValue())
                    .divide(totalWeight, 2, RoundingMode.HALF_UP);
                assigned = assigned.add(value).setScale(2, RoundingMode.HALF_UP);
            }
            evalItems.put(entry.getKey(), value);
            currentIndex++;
        }

        try {
            return objectMapper.writeValueAsString(evalItems);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize eval items.", exception);
        }
    }

    private String normalizeDomainCategory(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private void validateLevel2Submitted(Long evaluateeId, Long evaluationPeriodId) {
        QualitativeEvaluation level2 = findByLevel(evaluateeId, evaluationPeriodId, 2L);
        if (level2.getStatus() != QualEvalStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.EVALUATION_LEVEL2_NOT_SUBMITTED);
        }
    }

    private void validateLevel1Submitted(Long evaluateeId, Long evaluationPeriodId) {
        QualitativeEvaluation level1 = findByLevel(evaluateeId, evaluationPeriodId, 1L);
        if (level1.getStatus() != QualEvalStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.EVALUATION_LEVEL1_NOT_SUBMITTED);
        }
    }

    private QualitativeEvaluation findByLevel(Long evaluateeId, Long evaluationPeriodId, Long level) {
        return repository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(evaluateeId, evaluationPeriodId, level)
            .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
    }

    private Double requireScore(QualitativeEvaluation evaluation, String label) {
        if (evaluation.getScore() == null) {
            throw new BusinessException(
                ErrorCode.INVALID_INPUT,
                label + " 평가 점수가 산출되지 않아 최종 확정을 진행할 수 없습니다."
            );
        }
        return evaluation.getScore();
    }

    private void publishSubmittedEventAfterCommit(QualitativeEvaluation evaluation) {
        EvaluationPeriod evaluationPeriod = evaluationPeriodRepository.findById(evaluation.getEvaluationPeriodId())
            .orElseThrow(() -> new IllegalArgumentException("Evaluation period was not found."));

        QualitativeEvaluationSubmittedEvent event = new QualitativeEvaluationSubmittedEvent(
            evaluation.getQualitativeEvaluationId(),
            evaluation.getEvaluationPeriodId(),
            evaluationPeriod.getAlgorithmVersionId(),
            evaluation.getEvaluateeId(),
            evaluation.getEvaluatorId(),
            evaluation.getEvaluationLevel(),
            resolveSecondEvaluationMode(evaluation),
            resolveBaseRawScore(evaluation),
            evaluation.getEvalComment(),
            evaluation.getInputMethod() != null ? evaluation.getInputMethod().name() : null,
            DEFAULT_ANALYSIS_VERSION,
            evaluation.getStatus().name(),
            LocalDateTime.now(),
            loadKeywordRuleSnapshot()
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
    private String resolveSecondEvaluationMode(QualitativeEvaluation evaluation) {
        if (evaluation.getEvaluationLevel() != null && evaluation.getEvaluationLevel() == 2L) {
            return hasText(evaluation.getEvalComment()) ? "ANALYZE_COMMENT" : "KEEP_FIRST_SCORE";
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private BigDecimal resolveBaseRawScore(QualitativeEvaluation evaluation) {
        if (evaluation.getEvaluationLevel() == null || evaluation.getEvaluationLevel() < 2L) {
            return null;
        }

        QualitativeEvaluation firstEvaluation = findByLevel(
            evaluation.getEvaluateeId(),
            evaluation.getEvaluationPeriodId(),
            1L
        );
        Double baseScore = firstEvaluation.getScore();
        if (baseScore == null) {
            throw new BusinessException(
                ErrorCode.INVALID_INPUT,
                "1차 평가 점수가 산출되지 않아 2차 평가를 제출할 수 없습니다."
            );
        }
        return BigDecimal.valueOf(baseScore).setScale(4, RoundingMode.HALF_UP);
    }

    private List<QualitativeKeywordRuleEvent> loadKeywordRuleSnapshot() {
        return adminClient.getActiveDomainKeywordRules().stream()
            .filter(rule -> Boolean.TRUE.equals(rule.getDomainIsActive()))
            .map(this::toKeywordRuleEvent)
            .filter(Objects::nonNull)
            .toList();
    }

    private QualitativeKeywordRuleEvent toKeywordRuleEvent(DomainKeywordRuleResponse response) {
        if (response.getDomainKeyword() == null || response.getDomainKeyword().isBlank()) {
            return null;
        }

        BigDecimal baseScore = response.getDomainBaseScore();
        BigDecimal weight = response.getDomainWeight();
        if (baseScore == null || weight == null) {
            return null;
        }

        BigDecimal scoreWeight = baseScore.multiply(weight).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        if (scoreWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return new QualitativeKeywordRuleEvent(
            response.getDomainKeywordId(),
            response.getDomainKeyword(),
            response.getDomainCompetencyCategory(),
            scoreWeight
        );
    }

    private String toJson(List<?> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize matched keywords.", exception);
        }
    }

    private <T> T requireValue(T value, String fieldName) {
        if (value == null) {
            throw new IllegalStateException("Missing required field: " + fieldName);
        }
        return value;
    }
}
