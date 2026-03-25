package com.study.shop.domain.post.dto;

import com.study.shop.domain.post.entity.PostFile;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
public class PostFileResponseDto {
    private Long id;
    private String originalFileName;
    private String filePath;
    private Long fileSize;

    public static PostFileResponseDto from(PostFile postFile) {
        return PostFileResponseDto.builder()
                .id(postFile.getId())
                .originalFileName(postFile.getOriginalFileName())
                .filePath(postFile.getFilePath())
                .fileSize(postFile.getFileSize())
                .build();
    }
}
