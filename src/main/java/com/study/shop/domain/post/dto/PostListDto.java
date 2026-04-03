package com.study.shop.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListDto implements Serializable {
    private Long id;
    private String title;
    private String writer;
    private LocalDateTime createTime;
    private Long commentCount;
    private int viewCount;
}
