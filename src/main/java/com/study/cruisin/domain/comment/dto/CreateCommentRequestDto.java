package com.study.cruisin.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommentRequestDto {
    @NotNull
    private Long postId;

    @NotBlank
    private String writer;

    @NotBlank
    private String content;
}
