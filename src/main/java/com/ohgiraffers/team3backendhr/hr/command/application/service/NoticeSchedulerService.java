package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeSchedulerService {

    private final NoticeRepository noticeRepository;

    /**
     * 예약 게시 전환: publishStartAt이 현재 시각 이전인 RESERVATION 공지를 POSTING으로 변경
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void publishScheduledNotices() {
        List<Notice> targets = noticeRepository
                .findByNoticeStatusAndPublishStartAtLessThanEqual(
                        NoticeStatus.RESERVATION, LocalDateTime.now());
        targets.forEach(Notice::publish);
    }

    /**
     * 중요 공지 만료: important_end_at이 지난 중요 공지의 is_important를 0으로 변경
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireImportantNotices() {
        List<Notice> targets = noticeRepository
                .findByIsImportantAndImportantEndAtLessThan(1, LocalDateTime.now());
        targets.forEach(Notice::expireImportant);
    }
}
