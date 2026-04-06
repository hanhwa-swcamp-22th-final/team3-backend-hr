package com.ohgiraffers.team3backendhr.hr.command.application.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class NoticeDraftRequest {

    private final String noticeTitle;

    private final String noticeContent;

    private final boolean isImportant;

    private final LocalDateTime importantEndAt;
}
