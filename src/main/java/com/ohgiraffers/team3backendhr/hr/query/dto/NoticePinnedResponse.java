package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NoticePinnedResponse {

    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private LocalDateTime createdAt;
    private LocalDateTime importantEndAt;
}
