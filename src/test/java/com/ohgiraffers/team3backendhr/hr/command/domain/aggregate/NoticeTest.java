package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class NoticeTest {

    private Notice baseNotice() {
        return Notice.builder()
                .noticeId(1L)
                .employeeId(10L)
                .noticeStatus(NoticeStatus.TEMPORARY)
                .noticeTitle("원래 제목")
                .noticeContent("원래 내용")
                .build();
    }

    @Test
    @DisplayName("공지 update 호출 시 제목·내용·상태·중요여부·기간이 갱신된다")
    void update_changesAllFields() {
        Notice notice = baseNotice();
        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 4, 30, 23, 59);

        notice.update("새 제목", "새 내용", NoticeStatus.POSTING, true, start, end);

        assertThat(notice.getNoticeTitle()).isEqualTo("새 제목");
        assertThat(notice.getNoticeContent()).isEqualTo("새 내용");
        assertThat(notice.getNoticeStatus()).isEqualTo(NoticeStatus.POSTING);
        assertThat(notice.getIsImportant()).isEqualTo(1);
        assertThat(notice.getPublishStartAt()).isEqualTo(start);
        assertThat(notice.getImportantEndAt()).isEqualTo(end);
    }

    @Test
    @DisplayName("important=false 로 update 하면 isImportant 가 0 이 된다")
    void update_notImportant_setsZero() {
        Notice notice = baseNotice();

        notice.update("제목", "내용", NoticeStatus.POSTING, false, null, null);

        assertThat(notice.getIsImportant()).isEqualTo(0);
    }

    @Test
    @DisplayName("incrementViews 를 호출할 때마다 조회수가 1 씩 증가한다")
    void incrementViews_increasesCountByOne() {
        Notice notice = baseNotice();
        assertThat(notice.getNoticeViews()).isEqualTo(0L);

        notice.incrementViews();
        notice.incrementViews();
        notice.incrementViews();

        assertThat(notice.getNoticeViews()).isEqualTo(3L);
    }
}
