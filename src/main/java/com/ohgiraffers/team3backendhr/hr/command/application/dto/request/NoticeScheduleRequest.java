package com.ohgiraffers.team3backendhr.hr.command.application.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class NoticeScheduleRequest {

    @NotBlank(message = "공지 제목은 필수입니다.")
    private final String noticeTitle;

    @NotBlank(message = "공지 내용은 필수입니다.")
    private final String noticeContent;

    private final boolean isImportant;

    private final LocalDateTime importantEndAt;

    @NotNull(message = "예약 게시 시각은 필수입니다.")
    @Future(message = "예약 게시 시각은 현재 시각 이후여야 합니다.")
    private final LocalDateTime publishStartAt;
}
