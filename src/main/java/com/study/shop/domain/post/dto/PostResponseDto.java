package com.study.shop.domain.post.dto;

import com.study.shop.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getWriter())
                .createdAt(post.getCreatedAt() != null ? post.getCreatedAt() : null)
                .updatedAt(post.getUpdatedAt() != null ? post.getUpdatedAt() : null)
                .build();
    }
}
