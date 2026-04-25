package com.study.shop.domain.post.dto;

import com.study.shop.domain.post.entity.Post;
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

    public static PostListDto from(Post post) {
        return PostListDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .writer(post.getMember().getEmail())
                .createTime(post.getCreatedAt())
                .commentCount((long) post.getComments().size())
                .viewCount(post.getViewCount())
                .build();
    }
}
