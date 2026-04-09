package com.ohgiraffers.team3backendhr.infrastructure.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileDetail {
    private String fileName;
    private String filePath;
    private Long fileSize;
}
