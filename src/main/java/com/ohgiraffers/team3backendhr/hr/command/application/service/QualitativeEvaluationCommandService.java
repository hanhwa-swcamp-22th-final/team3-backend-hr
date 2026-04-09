package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationConfirmRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.qualitativeevaluation.QualitativeEvaluationSubmitRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationcomment.EvaluationComment;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationperiod.EvaluationPeriod;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationCommentRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.EvaluationPeriodRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import com.ohgiraffers.team3backendhr.infrastructure.client.AdminClient;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.DomainKeywordRuleResponse;
import com.ohgiraffers.team3backendhr.infrastructure.client.dto.WorkerResponse;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationAnalyzedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationNormalizedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeEvaluationSubmittedEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeKeywordRuleEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.dto.QualitativeSentenceAnalysisEvent;
import com.ohgiraffers.team3backendhr.infrastructure.kafka.publisher.QualitativeEvaluationEventPublisher;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private final QualitativeEvaluationEventPublisher qualitativeEvaluationEventPublisher;
    private final ObjectMapper objectMapper;

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

    public void saveDraftForDL(Long evaluatorId, Long evaluateeId, QualitativeEvaluationDraftRequest request) {
        validateLevel1Submitted(evaluateeId, request.getEvaluationPeriodId());
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 2L);
        eval.saveDraft(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
    }

    public void submitForDL(Long evaluatorId, Long evaluateeId, QualitativeEvaluationSubmitRequest request) {
        validateLevel1Submitted(evaluateeId, request.getEvaluationPeriodId());
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 2L);
        eval.submit(evaluatorId, request.getEvalItems(), request.getEvalComment(), request.getInputMethod());
        publishSubmittedEventAfterCommit(eval);
    }

    public void applyAnalyzedResult(QualitativeEvaluationAnalyzedEvent event) {
        BigDecimal rawScore = requireValue(event.getRawScore(), "rawScore");

        QualitativeEvaluation eval = repository.findById(event.getQualitativeEvaluationId())
            .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
        eval.applyAnalysisResult(rawScore.doubleValue());
        replaceEvaluationComments(event);
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
        QualitativeEvaluation eval = findByLevel(evaluateeId, request.getEvaluationPeriodId(), 3L);
        eval.confirmFinal(evaluatorId, request.getEvalComment(), request.getInputMethod());
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
            .contextWeight(sentenceAnalysis.getContextWeight())
            .negationDetected(Boolean.TRUE.equals(sentenceAnalysis.getNegationDetected()))
            .createdAt(occurredAt)
            .updatedAt(occurredAt)
            .build();
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
            return "ANALYZE_COMMENT";
        }
        return null;
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
            throw new IllegalStateException("Level 1 raw score is required before submitting level 2 qualitative evaluation.");
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

        return new QualitativeKeywordRuleEvent(response.getDomainKeyword(), scoreWeight);
    }

    private String toJson(List<String> values) {
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
