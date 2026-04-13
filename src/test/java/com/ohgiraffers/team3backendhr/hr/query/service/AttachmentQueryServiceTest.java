package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachment.Attachment;
import com.ohgiraffers.team3backendhr.hr.query.mapper.NoticeQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AttachmentQueryServiceTest {

    @Mock
    private NoticeQueryMapper noticeQueryMapper;

    @InjectMocks
    private AttachmentQueryService attachmentQueryService;

    @Test
    @DisplayName("존재하는 첨부파일 ID로 조회 시 파일 정보를 반환한다")
    void getAttachmentDetail_found_returnsAttachment() {
        // given
        Long attachmentId = 3000L;
        Attachment attachment = Attachment.builder()
                .attachmentId(attachmentId)
                .fileName("test.txt")
                .filePath("/uploads/notices/test.txt")
                .build();
        given(noticeQueryMapper.findAttachmentById(attachmentId)).willReturn(attachment);

        // when
        Attachment result = attachmentQueryService.getAttachmentDetail(attachmentId);

        // then
        assertThat(result.getFileName()).isEqualTo("test.txt");
        assertThat(result.getFilePath()).isEqualTo("/uploads/notices/test.txt");
    }

    @Test
    @DisplayName("존재하지 않는 첨부파일 ID로 조회 시 예외가 발생한다")
    void getAttachmentDetail_notFound_throwsException() {
        // given
        given(noticeQueryMapper.findAttachmentById(9999L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> attachmentQueryService.getAttachmentDetail(9999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("첨부파일을 찾을 수 없습니다.");
    }
}
