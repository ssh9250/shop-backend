package com.study.cruisin.domain.board.dto;

import com.study.cruisin.support.util.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    public static class CreateRequest {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
        @NotBlank
        private String writer;
    }

    public static class Response extends BaseEntity {
        private Long id;
        private String title;
        private String content;
        private String writer;
    }
}
