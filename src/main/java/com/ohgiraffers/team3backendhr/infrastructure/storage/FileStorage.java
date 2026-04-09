package com.ohgiraffers.team3backendhr.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    FileDetail upload(MultipartFile file, String directory);
    void delete(String filePath);
}
