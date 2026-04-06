package com.ohgiraffers.team3backendhr.hr.command.application.service;

import com.ohgiraffers.team3backendhr.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeCreateRequest;
import com.ohgiraffers.team3backendhr.hr.command.application.dto.request.NoticeUpdateRequest;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.Notice;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeStatus;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeTarget;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.NoticeTargetRole;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeRepository;
import com.ohgiraffers.team3backendhr.hr.command.domain.repository.NoticeTargetRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeCommandServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeTargetRepository noticeTargetRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private NoticeCommandService noticeCommandService;

    @Nested
    @DisplayName("createNotice 메서드")
    class CreateNotice {

        @Test
        @DisplayName("요청 정보가 포함된 공지가 저장된다")
        void createNotice_savesNoticeWithRequestInfo() {
            // given
            NoticeCreateRequest request = new NoticeCreateRequest(
                    NoticeStatus.PUBLISHED,
                    true,
                    "공지 제목",
                    "공지 내용",
                    LocalDateTime.of(2026, 4, 1, 9, 0),
                    LocalDateTime.of(2026, 4, 30, 23, 59),
                    List.of(NoticeTargetRole.WORKER)
            );
            given(idGenerator.generate()).willReturn(1000L, 2000L);
            given(noticeRepository.save(any(Notice.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            noticeCommandService.createNotice(request, 99L);

            // then
            ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
            verify(noticeRepository).save(captor.capture());

            Notice saved = captor.getValue();
            assertThat(saved.getNoticeId()).isEqualTo(1000L);
            assertThat(saved.getEmployeeId()).isEqualTo(99L);
            assertThat(saved.getNoticeStatus()).isEqualTo(NoticeStatus.PUBLISHED);
            assertThat(saved.getIsImportant()).isEqualTo(1);
            assertThat(saved.getNoticeTitle()).isEqualTo("공지 제목");
            assertThat(saved.getNoticeContent()).isEqualTo("공지 내용");
        }

        @Test
        @DisplayName("대상 역할 수만큼 NoticeTarget이 저장된다")
        void createNotice_savesTargetsForEachRole() {
            // given
            NoticeCreateRequest request = new NoticeCreateRequest(
                    NoticeStatus.PUBLISHED,
                    false,
                    "공지 제목",
                    "공지 내용",
                    null,
                    null,
                    List.of(NoticeTargetRole.WORKER, NoticeTargetRole.TEAM_LEADER)
            );
            given(idGenerator.generate()).willReturn(1000L, 2000L, 3000L);
            given(noticeRepository.save(any(Notice.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            noticeCommandService.createNotice(request, 99L);

            // then
            ArgumentCaptor<NoticeTarget> captor = ArgumentCaptor.forClass(NoticeTarget.class);
            verify(noticeTargetRepository, times(2)).save(captor.capture());

            List<NoticeTarget> savedTargets = captor.getAllValues();
            assertThat(savedTargets).hasSize(2);
            assertThat(savedTargets.get(0).getNoticeId()).isEqualTo(1000L);
            assertThat(savedTargets.get(0).getTargetRole()).isEqualTo(NoticeTargetRole.WORKER);
            assertThat(savedTargets.get(1).getTargetRole()).isEqualTo(NoticeTargetRole.TEAM_LEADER);
        }

        @Test
        @DisplayName("important=false 이면 isImportant 값이 0으로 저장된다")
        void createNotice_notImportant_savesIsImportantZero() {
            // given
            NoticeCreateRequest request = new NoticeCreateRequest(
                    NoticeStatus.DRAFT,
                    false,
                    "공지 제목",
                    "공지 내용",
                    null,
                    null,
                    List.of(NoticeTargetRole.WORKER)
            );
            given(idGenerator.generate()).willReturn(1000L, 2000L);
            given(noticeRepository.save(any(Notice.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            noticeCommandService.createNotice(request, 99L);

            // then
            ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
            verify(noticeRepository).save(captor.capture());
            assertThat(captor.getValue().getIsImportant()).isEqualTo(0);
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
                    .noticeStatus(NoticeStatus.DRAFT)
                    .noticeTitle("기존 제목")
                    .noticeContent("기존 내용")
                    .build();

            NoticeUpdateRequest request = new NoticeUpdateRequest(
                    NoticeStatus.PUBLISHED,
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
            assertThat(existing.getNoticeStatus()).isEqualTo(NoticeStatus.PUBLISHED);
            assertThat(existing.getIsImportant()).isEqualTo(1);
            assertThat(existing.getNoticeTitle()).isEqualTo("수정된 제목");
            assertThat(existing.getNoticeContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("존재하지 않는 공지 수정 시 예외가 발생한다")
        void updateNotice_notFound_throwsException() {
            // given
            NoticeUpdateRequest request = new NoticeUpdateRequest(
                    NoticeStatus.PUBLISHED, false, "제목", "내용", null, null
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
        @DisplayName("공지와 대상 역할이 모두 삭제된다")
        void deleteNotice_deletesNoticeAndTargets() {
            // given
            Notice existing = Notice.builder()
                    .noticeId(1000L)
                    .employeeId(99L)
                    .noticeStatus(NoticeStatus.PUBLISHED)
                    .noticeTitle("제목")
                    .noticeContent("내용")
                    .build();

            List<NoticeTarget> targets = List.of(
                    NoticeTarget.builder().noticeTargetId(2000L).noticeId(1000L).targetRole(NoticeTargetRole.WORKER).build()
            );

            given(noticeRepository.findById(1000L)).willReturn(Optional.of(existing));
            given(noticeTargetRepository.findByNoticeId(1000L)).willReturn(targets);

            // when
            noticeCommandService.deleteNotice(1000L);

            // then
            verify(noticeTargetRepository).deleteAll(targets);
            verify(noticeRepository).delete(existing);
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
