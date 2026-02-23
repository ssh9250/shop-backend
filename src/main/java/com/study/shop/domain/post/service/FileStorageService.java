package com.study.shop.domain.post.service;

import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.post.entity.PostFile;
import lombok.RequiredArgsConstructor;
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
// todo: 전체적으로 재확인
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

    public void delteFile(String storedFileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(storedFileName);
            Files.deleteIfExists(filePath);
        }
        catch (Exception e) {
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다: " + storedFileName);
        }
    }
}
