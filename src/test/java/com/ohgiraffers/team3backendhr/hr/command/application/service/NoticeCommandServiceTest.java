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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeCommandServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private AttachmentFileGroupRepository attachmentFileGroupRepository;

    @Mock
    private FileStorage fileStorage;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private NoticeCommandService noticeCommandService;

    @Nested
    @DisplayName("uploadAttachments 메서드 (첨부파일 업로드)")
    class UploadAttachments {

        @Test
        @DisplayName("공지사항에 첨부파일을 업로드하면 파일 그룹과 첨부파일이 저장된다")
        void uploadAttachments_savesFileGroupAndAttachments() {
            // given
            Long noticeId = 1000L;
            List<MultipartFile> files = List.of(
                    new MockMultipartFile("files", "test1.txt", "text/plain", "content1".getBytes()),
                    new MockMultipartFile("files", "test2.png", "image/png", "content2".getBytes())
            );

            Notice notice = Notice.builder().noticeId(noticeId).build();
            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));
            given(idGenerator.generate()).willReturn(2000L, 3001L, 3002L);
            
            given(fileStorage.upload(any(MultipartFile.class), eq("notices")))
                    .willReturn(new FileDetail("test1.txt", "/path/test1.txt", 100L));

            // when
            noticeCommandService.uploadAttachments(noticeId, files);

            // then
            verify(attachmentFileGroupRepository).save(any(AttachmentFileGroup.class));
            verify(attachmentRepository, times(2)).save(any(Attachment.class));
            verify(fileStorage, times(2)).upload(any(MultipartFile.class), eq("notices"));
        }
    }

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
        }
    }

    @Nested
    @DisplayName("deleteNotice 메서드")
    class DeleteNotice {

        @Test
        @DisplayName("Soft Delete — 공지가 삭제되면 연관된 첨부파일들도 bulk softDelete가 호출된다")
        void deleteNotice_softDeletesNoticeAndAttachments() {
            // given
            Long noticeId = 1000L;
            Long fileGroupId = 2000L;
            Notice existing = Notice.builder()
                    .noticeId(noticeId)
                    .fileGroupId(fileGroupId)
                    .noticeStatus(NoticeStatus.POSTING)
                    .build();

            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(existing));

            // when
            noticeCommandService.deleteNotice(noticeId);

            // then
            assertThat(existing.getIsDeleted()).isEqualTo(1);
            verify(attachmentRepository).deleteByFileGroupId(fileGroupId);
        }
    }
}
