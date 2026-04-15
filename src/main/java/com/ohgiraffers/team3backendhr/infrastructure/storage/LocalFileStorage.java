package com.ohgiraffers.team3backendhr.infrastructure.storage;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalFileStorage implements FileStorage {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png", "gif",
            "xlsx", "xls",
            "txt", "mp4", "wav", "mp3"
    );

    @Override
    public FileDetail upload(MultipartFile file, String directory) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "파일이 비어있습니다.");
        }

        // 1. 확장자 추출 및 검증
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "허용되지 않는 파일 형식입니다: " + extension);
        }

        try {
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path root = resolveTargetDirectory(uploadRoot, directory);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            // 3. 파일명 중복 방지 (UUID 사용) - extension에 점(.) 포함하여 다시 추출
            String dotExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                dotExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String savedFileName = UUID.randomUUID().toString() + dotExtension;
            Path targetPath = root.resolve(savedFileName).normalize();
            if (!targetPath.startsWith(root)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "유효하지 않은 파일 경로입니다.");
            }

            // 4. 파일 저장
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return new FileDetail(
                    originalFileName,
                    targetPath.toString(),
                    file.getSize()
            );

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다.");
        }
    }

    private Path resolveTargetDirectory(Path uploadRoot, String directory) {
        if (directory == null || directory.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "저장 디렉터리는 필수입니다.");
        }

        Path resolved = uploadRoot.resolve(directory).normalize();
        if (!resolved.startsWith(uploadRoot)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "유효하지 않은 저장 경로입니다.");
        }
        return resolved;
    }

    @Override
    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            // 삭제 실패는 로그만 남기고 무시하거나 예외 처리
        }
    }
}
