package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeListResponse {

    private Long noticeId;
    private String noticeTitle;
    private Long authorId;          // Admin Feign 없이 employeeId로 임시 반환
    private String noticeStatus;
    private Integer isImportant;
    private Long noticeViews;
    private LocalDateTime createdAt;
    private LocalDateTime publishStartAt;
    private LocalDateTime importantEndAt;
}
