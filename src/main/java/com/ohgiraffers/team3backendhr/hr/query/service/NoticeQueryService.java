package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticePinnedResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.NoticeQueryMapper;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachment.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeQueryService {

    private final NoticeQueryMapper noticeQueryMapper;

    public List<NoticeListResponse> getNotices(String keyword, Boolean isImportant,
                                                String status, int page, int size) {
        return noticeQueryMapper.findNotices(keyword, status, isImportant, page * size, size);
    }

    public NoticeDetailResponse getNoticeDetail(Long noticeId) {
        NoticeDetailResponse response = noticeQueryMapper.findById(noticeId);
        if (response == null) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
        }
        return response;
    }

    /* 첨부파일 상세 정보 조회 (Query 전용) */
    public Attachment getAttachmentDetail(Long attachmentId) {
        Attachment attachment = noticeQueryMapper.findAttachmentById(attachmentId);
        if (attachment == null) {
            throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }
        return attachment;
    }

    public NoticePinnedResponse getPinnedNotice() {
        return noticeQueryMapper.findPinned();
    }
}
