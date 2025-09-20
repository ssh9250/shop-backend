package com.study.shop.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequestDto {
    @Schema(description = "수정할 제목")
    private String title;

    @Schema(description = "수정할 내용")
    private String content;
}
