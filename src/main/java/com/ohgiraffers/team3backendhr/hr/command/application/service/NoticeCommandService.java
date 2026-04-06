package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticePublishRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeScheduleRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeTarget;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeCommandService {

    private final NoticeRepository noticeRepository;
    private final NoticeTargetRepository noticeTargetRepository;
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

    /* 임시 저장 — status: TEMPORARY, 제목·내용 선택 */
    public void draftNotice(NoticeDraftRequest request, Long employeeId) {
        saveNotice(
                NoticeStatus.TEMPORARY,
                employeeId,
                request.getNoticeTitle(),
                request.getNoticeContent(),
                request.isImportant(),
                null,
                request.getImportantEndAt()
        );
    }

    public void updateNotice(Long noticeId, NoticeUpdateRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));
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

    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));
        List<NoticeTarget> targets = noticeTargetRepository.findByNoticeId(noticeId);
        noticeTargetRepository.deleteAll(targets);
        noticeRepository.delete(notice);
    }

    private void saveNotice(NoticeStatus status, Long employeeId,
                             String title, String content, boolean isImportant,
                             LocalDateTime publishStartAt, LocalDateTime importantEndAt) {
        Notice notice = Notice.builder()
                .noticeId(idGenerator.generate())
                .employeeId(employeeId)
                .noticeStatus(status)
                .isImportant(isImportant ? 1 : 0)
                .noticeTitle(title)
                .noticeContent(content)
                .publishStartAt(publishStartAt)
                .importantEndAt(importantEndAt)
                .build();
        noticeRepository.save(notice);
    }
}
