package com.study.cruisin.testutil;

import com.study.cruisin.domain.board.entity.Post;

public class PostTestHelper {
    public static Post createPost(String title, String content, String writer) {
        return Post.builder()
                .title(title)
                .content(content)
                .writer(writer)
                .build();
    }
}
