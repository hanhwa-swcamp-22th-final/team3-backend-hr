package com.ohgiraffers.team3backendhr.hr.command.infrastructure.repository;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.NoticeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaNoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByNoticeStatus(NoticeStatus status);
    List<Notice> findByEmployeeId(Long employeeId);

    /* 예약 게시 전환 대상: RESERVATION 이고 publishStartAt <= now */
    List<Notice> findByNoticeStatusAndPublishStartAtLessThanEqual(NoticeStatus status, LocalDateTime dateTime);

    /* 중요 공지 만료 대상: is_important = 1 이고 important_end_at < now */
    List<Notice> findByIsImportantAndImportantEndAtLessThan(int isImportant, LocalDateTime dateTime);
}
