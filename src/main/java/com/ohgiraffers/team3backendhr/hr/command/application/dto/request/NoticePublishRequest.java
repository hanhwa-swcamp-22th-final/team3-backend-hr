package com.ohgiraffers.team3backendhr.hr.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class NoticePublishRequest {

    @NotBlank(message = "공지 제목은 필수입니다.")
    private final String noticeTitle;

    @NotBlank(message = "공지 내용은 필수입니다.")
    private final String noticeContent;

    private final boolean isImportant;

    private final LocalDateTime importantEndAt;
}
