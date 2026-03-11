package com.study.shop.domain.post.dto;

import java.time.LocalDateTime;

public class PostListDto {
    private Long id;
    private String title;
    private String writer;
    private LocalDateTime createTime;
    private Long commentCount;
}
