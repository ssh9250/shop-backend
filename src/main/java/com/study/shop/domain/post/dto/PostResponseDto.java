package com.study.shop.domain.post.dto;

import com.study.shop.domain.post.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponseDto {
    @Schema(description = "게시글 id", example = "2")
    private Long id;

    @Schema(description = "게시글 제목", example = "제목")
    private String title;

    @Schema(description = "게시글 내용", example = "내용을 입력하세요.")
    private String content;

    @Schema(description = "작성자 닉네임", example = "writer")
    private String writer;

    @Schema(description = "생성 일시", format = "date-time", example = "2025-01-01T00:00:01")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", format = "date-time", example = "2025-01-01T00:00:01")
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
