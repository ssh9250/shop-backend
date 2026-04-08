package com.study.shop.domain.post.service;

import com.study.shop.domain.post.entity.PostFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public PostFile storeFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + "_" + originalFilename;

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(Path.of(uploadDir));
        }

        Path filePath = uploadPath.resolve(storedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return PostFile.builder()
                .originalFileName(originalFilename)
                .storedFileName(storedFileName)
                .filePath(uploadDir)
                .fileSize(file.getSize())
                .build();
    }

    public void deleteFile(String storedFileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(storedFileName);
            Files.deleteIfExists(filePath);
        }
        catch (Exception e) {
            log.error("파일 삭제 중 오류가 발생했습니다 : {}", storedFileName, e);
        }
    }
}
