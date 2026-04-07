package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeDraftRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticePublishRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeScheduleRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeCommandServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private NoticeCommandService noticeCommandService;

    @Nested
    @DisplayName("publishNotice 메서드 (즉시 게시)")
    class PublishNotice {

        @Test
        @DisplayName("즉시 게시 공지가 POSTING 상태로 저장된다")
        void publishNotice_savesWithPostingStatus() {
            // given
            NoticePublishRequest request = new NoticePublishRequest(
                    "공지 제목",
                    "공지 내용",
                    true,
                    LocalDateTime.of(2026, 4, 30, 23, 59)
            );
            given(idGenerator.generate()).willReturn(1000L);
            given(noticeRepository.save(any(Notice.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            noticeCommandService.publishNotice(request, 99L);

            // then
            ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
            verify(noticeRepository).save(captor.capture());

            Notice saved = captor.getValue();
            assertThat(saved.getNoticeId()).isEqualTo(1000L);
            assertThat(saved.getEmployeeId()).isEqualTo(99L);
            assertThat(saved.getNoticeStatus()).isEqualTo(NoticeStatus.POSTING);
            assertThat(saved.getIsImportant()).isEqualTo(1);
            assertThat(saved.getNoticeTitle()).isEqualTo("공지 제목");
            assertThat(saved.getNoticeContent()).isEqualTo("공지 내용");
        }
    }

    @Nested
    @DisplayName("scheduleNotice 메서드 (예약 게시)")
    class ScheduleNotice {

        @Test
        @DisplayName("예약 게시 공지가 RESERVATION 상태로 저장된다")
        void scheduleNotice_savesWithReservationStatus() {
            // given
            NoticeScheduleRequest request = new NoticeScheduleRequest(
                    "공지 제목",
                    "공지 내용",
                    false,
                    null,
                    LocalDateTime.of(2026, 5, 1, 9, 0)
            );
            given(idGenerator.generate()).willReturn(1000L);
            given(noticeRepository.save(any(Notice.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            noticeCommandService.scheduleNotice(request, 99L);

            // then
            ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
            verify(noticeRepository).save(captor.capture());

            Notice saved = captor.getValue();
            assertThat(saved.getNoticeStatus()).isEqualTo(NoticeStatus.RESERVATION);
            assertThat(saved.getPublishStartAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 9, 0));
        }

        @Test
        @DisplayName("중요 공지이고 예약 시각이 종료일보다 늦으면 예외 발생")
        void scheduleNotice_importantAndPublishAfterEndAt_throwsException() {
            // given — publishStartAt(5월10일) > importantEndAt(5월1일)
            NoticeScheduleRequest request = new NoticeScheduleRequest(
                    "공지 제목", "공지 내용",
                    true,
                    LocalDateTime.of(2026, 5, 1, 23, 59),   // importantEndAt
                    LocalDateTime.of(2026, 5, 10, 9, 0)      // publishStartAt (더 늦음)
            );

            // when & then
            assertThatThrownBy(() -> noticeCommandService.scheduleNotice(request, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("중요 공지 종료일보다 이전이어야 합니다");
        }

        @Test
        @DisplayName("중요 공지가 아니면 예약 시각이 종료일보다 늦어도 허용")
        void scheduleNotice_notImportant_noValidation() {
            // given — isImportant=false 이므로 날짜 검증 없음
            NoticeScheduleRequest request = new NoticeScheduleRequest(
                    "공지 제목", "공지 내용",
                    false,
                    LocalDateTime.of(2026, 5, 1, 23, 59),
                    LocalDateTime.of(2026, 5, 10, 9, 0)
            );
            given(idGenerator.generate()).willReturn(1000L);
            given(noticeRepository.save(any(Notice.class))).willAnswer(inv -> inv.getArgument(0));

            // when & then — 예외 없이 저장됨
            noticeCommandService.scheduleNotice(request, 99L);
            verify(noticeRepository).save(any(Notice.class));
        }
    }

    @Nested
    @DisplayName("draftNotice 메서드 (임시 저장)")
    class DraftNotice {

        @Test
        @DisplayName("신규 임시 저장 — TEMPORARY 상태로 저장되고 noticeId가 반환된다")
        void draftNotice_savesWithTemporaryStatusAndReturnsId() {
            // given
            NoticeDraftRequest request = new NoticeDraftRequest(null, null, null, false, null);
            given(idGenerator.generate()).willReturn(1000L);
            given(noticeRepository.save(any(Notice.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            Long returnedId = noticeCommandService.draftNotice(request, 99L);

            // then
            ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
            verify(noticeRepository).save(captor.capture());
            assertThat(captor.getValue().getNoticeStatus()).isEqualTo(NoticeStatus.TEMPORARY);
            assertThat(returnedId).isEqualTo(1000L);
        }

        @Test
        @DisplayName("noticeId 있으면 기존 임시 저장 공지를 업데이트하고 noticeId를 반환한다")
        void draftNotice_withNoticeId_updatesExistingDraft() {
            // given
            Notice existing = Notice.builder()
                    .noticeId(1000L).employeeId(99L)
                    .noticeStatus(NoticeStatus.TEMPORARY)
                    .noticeTitle("기존 제목").noticeContent("기존 내용")
                    .build();
            NoticeDraftRequest request = new NoticeDraftRequest(1000L, "수정 제목", "수정 내용", false, null);
            given(noticeRepository.findById(1000L)).willReturn(Optional.of(existing));

            // when
            Long returnedId = noticeCommandService.draftNotice(request, 99L);

            // then
            assertThat(existing.getNoticeTitle()).isEqualTo("수정 제목");
            assertThat(returnedId).isEqualTo(1000L);
        }

        @Test
        @DisplayName("important=false 이면 isImportant 값이 0으로 저장된다")
        void draftNotice_notImportant_savesIsImportantZero() {
            // given
            NoticeDraftRequest request = new NoticeDraftRequest(null, "제목", "내용", false, null);
            given(idGenerator.generate()).willReturn(1000L);
            given(noticeRepository.save(any(Notice.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            noticeCommandService.draftNotice(request, 99L);

            // then
            ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
            verify(noticeRepository).save(captor.capture());
            assertThat(captor.getValue().getIsImportant()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("incrementViews 메서드")
    class IncrementViews {

        @Test
        @DisplayName("POSTING 공지는 조회수가 증가한다")
        void incrementViews_posting_increasesCount() {
            Notice notice = Notice.builder()
                    .noticeId(1L).employeeId(99L)
                    .noticeStatus(NoticeStatus.POSTING)
                    .noticeTitle("제목").noticeContent("내용")
                    .build();
            given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

            noticeCommandService.incrementViews(1L);

            assertThat(notice.getNoticeViews()).isEqualTo(1L);
        }

        @Test
        @DisplayName("TEMPORARY 공지는 조회수가 증가하지 않는다")
        void incrementViews_temporary_doesNotIncrease() {
            Notice notice = Notice.builder()
                    .noticeId(2L).employeeId(99L)
                    .noticeStatus(NoticeStatus.TEMPORARY)
                    .noticeTitle("제목").noticeContent("내용")
                    .build();
            given(noticeRepository.findById(2L)).willReturn(Optional.of(notice));

            noticeCommandService.incrementViews(2L);

            assertThat(notice.getNoticeViews()).isEqualTo(0L);
        }

        @Test
        @DisplayName("RESERVATION 공지는 조회수가 증가하지 않는다")
        void incrementViews_reservation_doesNotIncrease() {
            Notice notice = Notice.builder()
                    .noticeId(3L).employeeId(99L)
                    .noticeStatus(NoticeStatus.RESERVATION)
                    .noticeTitle("제목").noticeContent("내용")
                    .build();
            given(noticeRepository.findById(3L)).willReturn(Optional.of(notice));

            noticeCommandService.incrementViews(3L);

            assertThat(notice.getNoticeViews()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("updateNotice 메서드")
    class UpdateNotice {

        @Test
        @DisplayName("공지 필드가 요청 정보로 수정된다")
        void updateNotice_updatesFields() {
            // given
            Notice existing = Notice.builder()
                    .noticeId(1000L)
                    .employeeId(99L)
                    .noticeStatus(NoticeStatus.TEMPORARY)
                    .noticeTitle("기존 제목")
                    .noticeContent("기존 내용")
                    .build();

            NoticeUpdateRequest request = new NoticeUpdateRequest(
                    NoticeStatus.POSTING,
                    true,
                    "수정된 제목",
                    "수정된 내용",
                    LocalDateTime.of(2026, 4, 1, 9, 0),
                    LocalDateTime.of(2026, 4, 30, 23, 59)
            );

            given(noticeRepository.findById(1000L)).willReturn(Optional.of(existing));

            // when
            noticeCommandService.updateNotice(1000L, request);

            // then
            assertThat(existing.getNoticeStatus()).isEqualTo(NoticeStatus.POSTING);
            assertThat(existing.getIsImportant()).isEqualTo(1);
            assertThat(existing.getNoticeTitle()).isEqualTo("수정된 제목");
            assertThat(existing.getNoticeContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("존재하지 않는 공지 수정 시 예외가 발생한다")
        void updateNotice_notFound_throwsException() {
            // given
            NoticeUpdateRequest request = new NoticeUpdateRequest(
                    NoticeStatus.POSTING, false, "제목", "내용", null, null
            );
            given(noticeRepository.findById(9999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeCommandService.updateNotice(9999L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("공지를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("deleteNotice 메서드")
    class DeleteNotice {

        @Test
        @DisplayName("Soft Delete — is_deleted = 1, deleted_at 설정된다")
        void deleteNotice_softDeletes() {
            // given
            Notice existing = Notice.builder()
                    .noticeId(1000L)
                    .employeeId(99L)
                    .noticeStatus(NoticeStatus.POSTING)
                    .noticeTitle("제목")
                    .noticeContent("내용")
                    .build();
            given(noticeRepository.findById(1000L)).willReturn(Optional.of(existing));

            // when
            noticeCommandService.deleteNotice(1000L);

            // then
            assertThat(existing.getIsDeleted()).isEqualTo(1);
            assertThat(existing.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 공지 삭제 시 예외가 발생한다")
        void deleteNotice_notFound_throwsException() {
            // given
            given(noticeRepository.findById(9999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeCommandService.deleteNotice(9999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("공지를 찾을 수 없습니다.");
        }
    }
}
