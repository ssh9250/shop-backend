package com.study.shop.domain.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
public class PostListDto implements Serializable {
    private Long id;
    private String title;
    private String writer;
    private LocalDateTime createTime;
    private Long commentCount;
    private int viewCount;
}
