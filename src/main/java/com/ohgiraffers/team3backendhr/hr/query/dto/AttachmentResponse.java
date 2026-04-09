package com.ohgiraffers.team3backendhr.hr.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long attachmentId;
    private String fileName;
    private Long fileSize;
}
