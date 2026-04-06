package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeTarget;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeTargetRole;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeCommandService {

    private final NoticeRepository noticeRepository;
    private final NoticeTargetRepository noticeTargetRepository;
    private final IdGenerator idGenerator;

    public void createNotice(NoticeCreateRequest request, Long employeeId) {
        Notice notice = Notice.builder()
                .noticeId(idGenerator.generate())
                .employeeId(employeeId)
                .noticeStatus(request.getNoticeStatus())
                .isImportant(request.isImportant() ? 1 : 0)
                .noticeTitle(request.getNoticeTitle())
                .noticeContent(request.getNoticeContent())
                .publishStartAt(request.getPublishStartAt())
                .importantEndAt(request.getImportantEndAt())
                .build();
        noticeRepository.save(notice);

        for (NoticeTargetRole role : request.getTargetRoles()) {
            NoticeTarget target = NoticeTarget.builder()
                    .noticeTargetId(idGenerator.generate())
                    .noticeId(notice.getNoticeId())
                    .targetRole(role)
                    .build();
            noticeTargetRepository.save(target);
        }
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

    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));
        List<NoticeTarget> targets = noticeTargetRepository.findByNoticeId(noticeId);
        noticeTargetRepository.deleteAll(targets);
        noticeRepository.delete(notice);
    }
}
