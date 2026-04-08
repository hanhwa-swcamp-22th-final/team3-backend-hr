package com.ohgiraffers.team3backendhr.hr.command.application.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class NoticeDraftRequest {

    /* 기존 임시 저장 글 재저장 시 사용. null이면 신규 생성 */
    private final Long noticeId;

    private final String noticeTitle;

    private final String noticeContent;

    private final boolean isImportant;

    private final LocalDateTime importantEndAt;
}
