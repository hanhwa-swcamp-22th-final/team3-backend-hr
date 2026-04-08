package com.ohgiraffers.team3backendhr.hr.command.application.dto.request;

import com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.notice.NoticeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class NoticeUpdateRequest {

    @NotNull(message = "공지 상태는 필수입니다.")
    private final NoticeStatus noticeStatus;

    private final boolean important;

    @NotBlank(message = "공지 제목은 필수입니다.")
    private final String noticeTitle;

    @NotBlank(message = "공지 내용은 필수입니다.")
    private final String noticeContent;

    private final LocalDateTime publishStartAt;

    private final LocalDateTime importantEndAt;
}
