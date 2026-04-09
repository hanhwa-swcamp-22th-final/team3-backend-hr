package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticePublishRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeScheduleRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachment.Attachment;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachment.FileType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachmentfilegroup.AttachmentFileGroup;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachmentfilegroup.ReferenceType;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.AttachmentFileGroupRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.AttachmentRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import com.ohgiraffers.team3backendhr.infrastructure.storage.FileDetail;
import com.ohgiraffers.team3backendhr.infrastructure.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeCommandService {

    private final NoticeRepository noticeRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentFileGroupRepository attachmentFileGroupRepository;
    private final FileStorage fileStorage;
    private final IdGenerator idGenerator;

    /* 즉시 게시 — status: POSTING */
    public void publishNotice(NoticePublishRequest request, Long employeeId) {
        saveNotice(
                NoticeStatus.POSTING,
                employeeId,
                request.getNoticeTitle(),
                request.getNoticeContent(),
                request.isImportant(),
                null,
                request.getImportantEndAt()
        );
    }

    /**
     * HR-018: 첨부파일 업로드
     * - @Transactional 적용으로 DB 작업 원자성 보장
     * - 확장자 기반 FileType 자동 판별 로직 추가
     */
    public void uploadAttachments(Long noticeId, List<MultipartFile> files) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        Long fileGroupId = notice.getFileGroupId();
        if (fileGroupId == null) {
            fileGroupId = idGenerator.generate();
            AttachmentFileGroup group = AttachmentFileGroup.builder()
                    .fileGroupId(fileGroupId)
                    .referenceId(noticeId)
                    .referenceType(ReferenceType.NOTICE)
                    .build();
            attachmentFileGroupRepository.save(group);
            notice.setFileGroupId(fileGroupId);
        }

        for (MultipartFile file : files) {
            // 인프라 계층(FileStorage)에 업로드 위임
            FileDetail detail = fileStorage.upload(file, "notices");

            Attachment attachment = Attachment.builder()
                    .attachmentId(idGenerator.generate())
                    .fileGroupId(fileGroupId)
                    .fileName(detail.getFileName())
                    .filePath(detail.getFilePath())
                    .fileSize(detail.getFileSize())
                    .fileType(FileType.fromExtension(detail.getFileName())) // 개선: 확장자 기반 판별
                    .fileAttachmentUploadedAt(LocalDateTime.now())
                    .build();
            Attachment saved = attachmentRepository.save(attachment);
        }
    }

    /* 예약 게시 — status: RESERVATION, publishStartAt 필수 */
    public void scheduleNotice(NoticeScheduleRequest request, Long employeeId) {
        if (request.isImportant()
                && request.getImportantEndAt() != null
                && request.getPublishStartAt().isAfter(request.getImportantEndAt())) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_TIME);
        }
        saveNotice(
                NoticeStatus.RESERVATION,
                employeeId,
                request.getNoticeTitle(),
                request.getNoticeContent(),
                request.isImportant(),
                request.getPublishStartAt(),
                request.getImportantEndAt()
            );
        }

    /* 임시 저장 */
    public Long draftNotice(NoticeDraftRequest request, Long employeeId) {
        if (request.getNoticeId() != null) {
            Notice notice = noticeRepository.findById(request.getNoticeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
            notice.updateDraft(
                    request.getNoticeTitle(),
                    request.getNoticeContent(),
                    request.isImportant(),
                    request.getImportantEndAt()
            );
            return notice.getNoticeId();
        }
        return saveNotice(
                NoticeStatus.TEMPORARY,
                employeeId,
                request.getNoticeTitle() != null ? request.getNoticeTitle() : "",
                request.getNoticeContent() != null ? request.getNoticeContent() : "",
                request.isImportant(),
                null,
                request.getImportantEndAt()
        );
    }

    public void updateNotice(Long noticeId, NoticeUpdateRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        if (request.getNoticeStatus() == NoticeStatus.RESERVATION
                && request.getPublishStartAt() == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_TIME_REQUIRED);
        }
        notice.update(
                request.getNoticeTitle(),
                request.getNoticeContent(),
                request.getNoticeStatus(),
                request.isImportant(),
                request.getPublishStartAt(),
                request.getImportantEndAt()
        );
    }

    /* 조회수 증가 */
    public void incrementViews(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        if (notice.getNoticeStatus() == NoticeStatus.POSTING) {
            notice.incrementViews();
        }
    }

    /* Soft Delete */
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        notice.softDelete();

        // 연관된 첨부파일 Soft Delete 처리
        if (notice.getFileGroupId() != null) {
            attachmentRepository.deleteByFileGroupId(notice.getFileGroupId());
        }
    }

    private Long saveNotice(NoticeStatus status, Long employeeId,
                            String title, String content, boolean isImportant,
                            LocalDateTime publishStartAt, LocalDateTime importantEndAt) {
        Long newId = idGenerator.generate();
        Notice notice = Notice.builder()
                .noticeId(newId)
                .employeeId(employeeId)
                .noticeStatus(status)
                .isImportant(isImportant ? 1 : 0)
                .noticeTitle(title)
                .noticeContent(content)
                .publishStartAt(publishStartAt)
                .importantEndAt(importantEndAt)
                .build();
        noticeRepository.save(notice);
        return newId;
    }
}
