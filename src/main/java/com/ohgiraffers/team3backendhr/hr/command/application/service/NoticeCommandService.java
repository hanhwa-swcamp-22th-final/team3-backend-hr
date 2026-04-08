package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticePublishRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeScheduleRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeCommandService {

    private final NoticeRepository noticeRepository;
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

    /* 예약 게시 — status: RESERVATION, publishStartAt 필수 */
    public void scheduleNotice(NoticeScheduleRequest request, Long employeeId) {
        if (request.isImportant()
                && request.getImportantEndAt() != null
                && request.getPublishStartAt().isAfter(request.getImportantEndAt())) {
            throw new IllegalArgumentException("예약 게시 시각은 중요 공지 종료일보다 이전이어야 합니다.");
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

    /* 임시 저장 — noticeId 있으면 기존 TEMPORARY 공지 재저장, 없으면 신규 생성.
     * 생성된(또는 기존) noticeId 반환 — 프론트에서 재저장 시 사용 */
    public Long draftNotice(NoticeDraftRequest request, Long employeeId) {
        if (request.getNoticeId() != null) {
            Notice notice = noticeRepository.findById(request.getNoticeId())
                    .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));
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
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));
        if (request.getNoticeStatus() == NoticeStatus.RESERVATION
                && request.getPublishStartAt() == null) {
            throw new IllegalArgumentException("예약 게시 시각은 필수입니다.");
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

    /* 조회수 증가 — POSTING 상태인 공지만 증가 (임시저장·예약 상태는 무시) */
    public void incrementViews(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));
        if (notice.getNoticeStatus() == NoticeStatus.POSTING) {
            notice.incrementViews();
        }
    }

    /* Soft Delete — is_deleted = 1, deleted_at = now() */
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));
        notice.softDelete();
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
