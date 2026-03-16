package com.study.shop.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostSearchConditionDto {
    private String title;
    private String writer;
    private String content;
    private Boolean hidden;
    private LocalDateTime from;
    private LocalDateTime to;
    // 검색어는 나중에
}
