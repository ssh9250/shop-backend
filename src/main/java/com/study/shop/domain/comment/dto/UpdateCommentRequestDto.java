package com.study.shop.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentRequestDto {
    @NotBlank
    private String content;
}
