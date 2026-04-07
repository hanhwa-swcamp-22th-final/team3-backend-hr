package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeDetailResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticeListResponse;
import com.ohgiraffers.team3backendhr.hr.query.dto.NoticePinnedResponse;
import com.ohgiraffers.team3backendhr.hr.query.mapper.NoticeQueryMapper;
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
            throw new IllegalArgumentException("공지를 찾을 수 없습니다.");
        }
        return response;
    }

    public NoticePinnedResponse getPinnedNotice() {
        return noticeQueryMapper.findPinned();
    }
}
