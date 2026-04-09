package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDetailResponse {

    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private Long authorId;          // Admin Feign 없이 employeeId로 임시 반환
    private String noticeStatus;
    private Integer isImportant;
    private Long noticeViews;
    private LocalDateTime createdAt;
    private LocalDateTime publishStartAt;
    private LocalDateTime importantEndAt;
    private List<AttachmentResponse> attachments;
}
