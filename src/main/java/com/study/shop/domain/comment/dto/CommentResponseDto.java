package com.study.shop.domain.comment.dto;

import com.study.shop.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponseDto {
    private Long id;
    private String writer;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponseDto from(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .writer(comment.getWriter())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt() : null)
                .updatedAt(comment.getUpdatedAt() != null ? comment.getUpdatedAt() : null)
                .build();
    }
}
