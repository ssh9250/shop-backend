package com.study.shop.testutil;

import com.study.shop.domain.post.entity.Post;

public class PostTestHelper {
    public static Post createPost(String title, String content, String writer) {
        return Post.builder()
                .title(title)
                .content(content)
                .writer(writer)
                .build();
    }
}
