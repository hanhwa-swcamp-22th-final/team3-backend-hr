package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NoticeSchedulerServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private NoticeSchedulerService noticeSchedulerService;

    @Test
    @DisplayName("publishScheduledNotices: RESERVATION 공지가 POSTING 으로 전환된다")
    void publishScheduledNotices_transitionsToPosting() {
        Notice reserved = Notice.builder()
                .noticeId(1L)
                .employeeId(10L)
                .noticeStatus(NoticeStatus.RESERVATION)
                .noticeTitle("예약 공지")
                .noticeContent("내용")
                .publishStartAt(LocalDateTime.now().minusMinutes(1))
                .build();

        given(noticeRepository.findByNoticeStatusAndPublishStartAtLessThanEqual(
                eq(NoticeStatus.RESERVATION), any(LocalDateTime.class)))
                .willReturn(List.of(reserved));

        noticeSchedulerService.publishScheduledNotices();

        assertThat(reserved.getNoticeStatus()).isEqualTo(NoticeStatus.POSTING);
    }

    @Test
    @DisplayName("publishScheduledNotices: 대상 공지가 없으면 아무것도 변경되지 않는다")
    void publishScheduledNotices_noTargets_doesNothing() {
        given(noticeRepository.findByNoticeStatusAndPublishStartAtLessThanEqual(
                eq(NoticeStatus.RESERVATION), any(LocalDateTime.class)))
                .willReturn(List.of());

        noticeSchedulerService.publishScheduledNotices();
        // no exception, no state change
    }

    @Test
    @DisplayName("expireImportantNotices: importantEndAt 이 지난 공지의 isImportant 가 0 이 된다")
    void expireImportantNotices_setsIsImportantToZero() {
        Notice important = Notice.builder()
                .noticeId(2L)
                .employeeId(10L)
                .noticeStatus(NoticeStatus.POSTING)
                .noticeTitle("중요 공지")
                .noticeContent("내용")
                .isImportant(1)
                .importantEndAt(LocalDateTime.now().minusDays(1))
                .build();

        given(noticeRepository.findByIsImportantAndImportantEndAtLessThan(
                eq(1), any(LocalDateTime.class)))
                .willReturn(List.of(important));

        noticeSchedulerService.expireImportantNotices();

        assertThat(important.getIsImportant()).isEqualTo(0);
    }

    @Test
    @DisplayName("expireImportantNotices: 대상 공지가 없으면 아무것도 변경되지 않는다")
    void expireImportantNotices_noTargets_doesNothing() {
        given(noticeRepository.findByIsImportantAndImportantEndAtLessThan(
                eq(1), any(LocalDateTime.class)))
                .willReturn(List.of());

        noticeSchedulerService.expireImportantNotices();
        // no exception, no state change
    }
}
