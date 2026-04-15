package com.ohgiraffers.team3backendhr.infrastructure.storage;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void upload_storesFileInsideConfiguredRoot() throws Exception {
        LocalFileStorage storage = new LocalFileStorage();
        ReflectionTestUtils.setField(storage, "uploadDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "evidence.pdf",
                "application/pdf",
                "test".getBytes()
        );

        FileDetail detail = storage.upload(file, "appeals");

        Path savedPath = Path.of(detail.getFilePath());
        assertThat(savedPath).startsWith(tempDir);
        assertThat(Files.exists(savedPath)).isTrue();
    }

    @Test
    void upload_rejectsPathTraversalDirectory() {
        LocalFileStorage storage = new LocalFileStorage();
        ReflectionTestUtils.setField(storage, "uploadDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "evidence.pdf",
                "application/pdf",
                "test".getBytes()
        );

        assertThatThrownBy(() -> storage.upload(file, "..\\outside"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("유효하지 않은 저장 경로입니다.");
    }
}
