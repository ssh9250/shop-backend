package com.study.shop.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequestDto {
    @NotBlank
    @Schema(description = "게시글 제목", example = "첫 번째 글")
    private String title;

    @NotBlank
    @Schema(description = "게시글 내용", example = "내용을 입력하세요.")
    private String content;
}
