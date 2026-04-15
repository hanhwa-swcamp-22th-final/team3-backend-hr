package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;

import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealRegisterRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealReviewRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.appeal.AppealUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachment.Attachment;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachmentfilegroup.AttachmentFileGroup;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.AppealStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.AppealType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.EvaluationAppeal;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.evaluationappeal.ReviewResult;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualEvalStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.AppealRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.AttachmentFileGroupRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.AttachmentRepository;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.qualitativeevaluation.QualitativeEvaluation;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.QualitativeEvaluationRepository;
import com.ohgiraffers.team3backendhr.infrastructure.storage.FileDetail;
import com.ohgiraffers.team3backendhr.infrastructure.storage.FileStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppealServiceTest {

    @Mock private AppealRepository appealRepository;
    @Mock private AttachmentFileGroupRepository fileGroupRepository;
    @Mock private AttachmentRepository attachmentRepository;
    @Mock private QualitativeEvaluationRepository qualitativeEvaluationRepository;
    @Mock private FileStorage fileStorage;
    @Mock private IdGenerator idGenerator;
    @Mock private NotificationCommandService notificationCommandService;

    @InjectMocks
    private AppealCommandService service;

    private EvaluationAppeal buildAppeal(AppealStatus status) {
        return EvaluationAppeal.builder()
                .appealId(1L)
                .appealEmployeeId(100L)
                .evaluationPeriodId(5L)
                .appealType(AppealType.SCORE_ERRORS)
                .title("점수 오류 이의신청")
                .content("평가 점수에 명백한 오류가 있습니다. 재검토 요청드립니다.")
                .status(status)
                .fileGroupId(99L)
                .build();
    }

    private QualitativeEvaluation buildConfirmedEval(LocalDateTime confirmedAt) {
        return QualitativeEvaluation.builder()
                .qualitativeEvaluationId(10L)
                .evaluateeId(100L)
                .evaluationPeriodId(5L)
                .evaluationLevel(2L)
                .status(QualEvalStatus.SUBMITTED)
                .updatedAt(confirmedAt)
                .build();
    }

    @Test
    @DisplayName("이의신청을 등록하면 파일 그룹이 생성되고 DB에 저장된다")
    void register_success() {
        AppealRegisterRequest request = new AppealRegisterRequest(
                5L, AppealType.SCORE_ERRORS,
                "점수 오류 이의신청", "평가 점수에 명백한 오류가 있습니다. 재검토 요청드립니다.");

        QualitativeEvaluation eval = buildConfirmedEval(LocalDateTime.now().minusDays(3));
        given(qualitativeEvaluationRepository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(100L, 5L, 1L))
                .willReturn(Optional.empty());
        given(qualitativeEvaluationRepository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(100L, 5L, 2L))
                .willReturn(Optional.of(eval));
        given(idGenerator.generate()).willReturn(99L, 1L);
        given(fileGroupRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(appealRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        Long appealId = service.register(100L, request);

        verify(fileGroupRepository).save(any(AttachmentFileGroup.class));
        verify(appealRepository).save(any(EvaluationAppeal.class));
        assertThat(appealId).isEqualTo(99L);
    }

    @Test
    @DisplayName("평가가 CONFIRMED 상태가 아니면 이의신청 등록 시 예외가 발생한다")
    void register_fail_notConfirmed() {
        AppealRegisterRequest request = new AppealRegisterRequest(
                5L, AppealType.SCORE_ERRORS,
                "점수 오류 이의신청", "평가 점수에 명백한 오류가 있습니다. 재검토 요청드립니다.");

        QualitativeEvaluation eval = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(10L)
                .evaluateeId(100L)
                .evaluationPeriodId(5L)
                .evaluationLevel(2L)
                .status(QualEvalStatus.CONFIRMED)
                .build();
        given(qualitativeEvaluationRepository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(100L, 5L, 1L))
                .willReturn(Optional.empty());
        given(qualitativeEvaluationRepository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(100L, 5L, 2L))
                .willReturn(Optional.of(eval));

        assertThatThrownBy(() -> service.register(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("2차 평가가 제출된 건에 대해서만 이의신청할 수 있습니다.");
    }

    @Test
    @DisplayName("결과 통보 후 7일이 지나면 이의신청 등록 시 예외가 발생한다")
    void register_fail_expired() {
        AppealRegisterRequest request = new AppealRegisterRequest(
                5L, AppealType.SCORE_ERRORS,
                "점수 오류 이의신청", "평가 점수에 명백한 오류가 있습니다. 재검토 요청드립니다.");

        QualitativeEvaluation eval = QualitativeEvaluation.builder()
                .qualitativeEvaluationId(10L)
                .evaluateeId(100L)
                .evaluationPeriodId(5L)
                .evaluationLevel(2L)
                .status(QualEvalStatus.SUBMITTED)
                .updatedAt(LocalDateTime.now().minusDays(8))
                .build();
        given(qualitativeEvaluationRepository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(100L, 5L, 1L))
                .willReturn(Optional.empty());
        given(qualitativeEvaluationRepository.findByEvaluateeIdAndEvaluationPeriodIdAndEvaluationLevel(100L, 5L, 2L))
                .willReturn(Optional.of(eval));

        assertThatThrownBy(() -> service.register(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("2차 평가 제출 후 7일이 지나 이의신청할 수 없습니다.");
    }

    @Test
    @DisplayName("이의신청을 수정한다")
    void update_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.SUBMITTED);
        given(appealRepository.findById(1L)).willReturn(Optional.of(appeal));

        AppealUpdateRequest request = new AppealUpdateRequest(
                AppealType.MISSING_ITEMS, "수정된 제목입니다", "수정된 내용입니다. 20자 이상 작성합니다.");

        assertThatThrownBy(() -> service.update(1L, 100L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("제출된 이의신청은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("본인의 이의신청이 아니면 수정 시 예외가 발생한다")
    void update_fail_notOwner() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.SUBMITTED);
        given(appealRepository.findById(1L)).willReturn(Optional.of(appeal));

        AppealUpdateRequest request = new AppealUpdateRequest(
                AppealType.MISSING_ITEMS, "수정된 제목입니다", "수정된 내용입니다. 20자 이상 작성합니다.");

        assertThatThrownBy(() -> service.update(1L, 999L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("본인의 이의신청만 수정할 수 있습니다.");
    }

    @Test
    @DisplayName("이의신청을 취소하면 삭제된다")
    void cancel_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.RECEIVING);
        given(appealRepository.findById(1L)).willReturn(Optional.of(appeal));

        service.cancel(1L, 100L);

        verify(appealRepository).delete(appeal);
    }

    @Test
    @DisplayName("본인의 이의신청이 아니면 취소 시 예외가 발생한다")
    void cancel_fail_notOwner() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.RECEIVING);
        given(appealRepository.findById(1L)).willReturn(Optional.of(appeal));

        assertThatThrownBy(() -> service.cancel(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("본인의 이의신청만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("승인 시 이의신청이 완료 상태로 변경된다")
    void approve_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.REVIEWING);

        given(appealRepository.findById(1L)).willReturn(Optional.of(appeal));
        AppealReviewRequest request = new AppealReviewRequest(ReviewResult.ACKNOWLEDGE, 85.0, "점수 오류 확인됨");

        service.approve(1L, 200L, request);

        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.COMPLETED);
        assertThat(appeal.getReviewResult()).isEqualTo(ReviewResult.ACKNOWLEDGE);
    }

    @Test
    @DisplayName("반려 시 이의신청이 COMPLETED·DISMISS로 변경된다")
    void reject_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.REVIEWING);
        given(appealRepository.findById(1L)).willReturn(Optional.of(appeal));

        service.reject(1L, 200L);

        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.COMPLETED);
        assertThat(appeal.getReviewResult()).isEqualTo(ReviewResult.DISMISS);
    }

    @Test
    @DisplayName("보류 시 이의신청이 REVIEWING 상태를 유지한다")
    void hold_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.REVIEWING);
        given(appealRepository.findById(1L)).willReturn(Optional.of(appeal));

        service.hold(1L, 200L);

        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.REVIEWING);
    }

    @Test
    @DisplayName("첨부파일 업로드 시 파일 저장 후 attachment가 저장된다")
    void uploadAttachments_success() {
        EvaluationAppeal appeal = buildAppeal(AppealStatus.SUBMITTED);
        MockMultipartFile file = new MockMultipartFile(
                "files", "evidence.pdf", "application/pdf", "test".getBytes());

        given(appealRepository.findById(1L)).willReturn(Optional.of(appeal));
        given(fileStorage.upload(any(), any())).willReturn(new FileDetail("evidence.pdf", "/tmp/evidence.pdf", 4L));
        given(idGenerator.generate()).willReturn(321L);
        given(attachmentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0, Attachment.class));

        service.uploadAttachments(1L, 100L, List.of(file));

        verify(fileStorage).upload(any(), any());
        verify(attachmentRepository).save(any(Attachment.class));
    }
}
