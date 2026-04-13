package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachment.Attachment;
import com.ohgiraffers.team3backendhr.hr.query.mapper.NoticeQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentQueryService {

    private final NoticeQueryMapper noticeQueryMapper;

    public Attachment getAttachmentDetail(Long attachmentId) {
        Attachment attachment = noticeQueryMapper.findAttachmentById(attachmentId);
        if (attachment == null) {
            throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }
        return attachment;
    }
}
